/* Implements the GroupClient Interface */

import java.util.*;
import java.io.*;

// include crypto librarys needed
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;
import org.bouncycastle.util.encoders.Hex;

// add pemobject and pemreader for parsing public/private keys
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class GroupClient extends Client implements GroupClientInterface {

    DataLogger gLog = new DataLogger("Group");
    StringWriter stackTraceString = new StringWriter();
    PrintWriter stackTrace = new PrintWriter(stackTraceString);
    private SecretKey sessionConfidentialityAESKey = null, sessionIntegrityAESKey = null;
    private SecureRandom sr = null;
	public Hashtable<String, ArrayList<String>> encKeyChainTable = null;
	public Hashtable<String, ArrayList<String>> intKeyChainTable = null;
    int counter;

    public GroupClient() {
        // tell Client's connect and disconnect where to log in case of error
        super.log = new DataLogger("Group");
    }

    public synchronized boolean authenticateUser(String username) {
        Security.addProvider(new BouncyCastleProvider());
        Envelope message = null, response = null;
        message = new Envelope("AUTH1");
        try {
            message.addObject(username); //Add user name string
            output.writeObject(message);

            //Get the response from the server
            response = (Envelope) input.readObject();
        } catch (Exception e) {
            return false;
        }

        // we got stage 1 of the T1 protocol
        if (response.getMessage().equals("1")) {
            byte[] randomChallenge = null;
            randomChallenge = Base64.getDecoder().decode(response.getBytes1());
            byte[] aesChalEncrypt = null;
            aesChalEncrypt = response.getBytes2();
            byte[] aesInteEncrypt = null;
            aesInteEncrypt = response.getBytes3();

            try {
                // open pem file for our private key and the group servers public key
                PemReader userPrivPemReader = new PemReader(new InputStreamReader(new FileInputStream("MyKeys/user-" + username + "-privK.pem")));
                PemObject userPrivPemObj = userPrivPemReader.readPemObject();
                byte[] privateBytes = userPrivPemObj.getContent();
                PKCS8EncodedKeySpec privEncoded = new PKCS8EncodedKeySpec(privateBytes);
                KeyFactory daFactory = KeyFactory.getInstance("RSA");
                PrivateKey privK = daFactory.generatePrivate(privEncoded);

                userPrivPemReader.close();

                // decrypt the aesSessionKey 
                Cipher cipher = Cipher.getInstance("RSA", "BC");
                cipher.init(Cipher.DECRYPT_MODE, privK);
                byte[] decryptedKeyBytes = cipher.doFinal(Base64.getDecoder().decode(aesChalEncrypt));
                byte[] decryptedKeyBytes2 = cipher.doFinal(Base64.getDecoder().decode(aesInteEncrypt));
                byte[] aesKeyBytes = new byte[32];
                byte[] aesIntBytes = new byte[32];
                System.arraycopy(decryptedKeyBytes, 0, aesKeyBytes, 0, aesKeyBytes.length);
                System.arraycopy(decryptedKeyBytes2, 0, aesIntBytes, 0, aesIntBytes.length);
                sessionConfidentialityAESKey = new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES");
                sessionIntegrityAESKey = new SecretKeySpec(aesIntBytes, 0, aesIntBytes.length, "AES");

                // create sha-256 hash of key bytes + integrity +challenge bytes
                byte[] concat = new byte[84];
                byte[] encodedSesAes = sessionConfidentialityAESKey.getEncoded();
                byte[] encodedIntegrity = sessionIntegrityAESKey.getEncoded();
                System.arraycopy(encodedSesAes, 0, concat, 0, encodedSesAes.length);//copying the encoded key, and the challenge into a concat byte array
                System.arraycopy(encodedIntegrity, 0, concat, encodedSesAes.length, encodedIntegrity.length);
                System.arraycopy(randomChallenge, 0, concat, (encodedSesAes.length + encodedIntegrity.length), randomChallenge.length);
                MessageDigest theDigester = MessageDigest.getInstance("SHA-256", "BC");
                theDigester.update(concat);
                byte[] hashToSend = theDigester.digest();

                // create a second random challenge
                SecureRandom rand = new SecureRandom();
                byte[] randomSent = new byte[20];//160 bit random challenge.
                rand.nextBytes(randomSent);
                byte[] encryptedRandom = null;
                // encrypt it with the group servers public key
                try {
                    PemReader daPubPemReader = new PemReader(new InputStreamReader(new FileInputStream("MyKeys/gs-pubK.pem")));
                    PemObject daPubPemObj = daPubPemReader.readPemObject();
                    byte[] publicBytes = daPubPemObj.getContent();
                    X509EncodedKeySpec pubEncoded = new X509EncodedKeySpec(publicBytes);
                    KeyFactory daFactory2 = KeyFactory.getInstance("RSA");
                    PublicKey pubK = daFactory2.generatePublic(pubEncoded);

                    daPubPemReader.close();

                    cipher = Cipher.getInstance("RSA", "BC");
                    cipher.init(Cipher.ENCRYPT_MODE, pubK);
                    encryptedRandom = cipher.doFinal(randomSent);

                } catch (Exception e) {
                    System.out.println("Failed to load the group server's public key from MyKeys directory");
                    return false;
                }

                message = new Envelope("AUTH2");
                message.addBytes1(Base64.getEncoder().encode(hashToSend));
                message.addBytes2(Base64.getEncoder().encode(encryptedRandom));
                output.writeObject(message);

                //Get the response from the server
                response = (Envelope) input.readObject();

                // if random challenge back is the same, its the server
                if (response.getMessage().equals("2")) {
                    byte[] randomBack = Base64.getDecoder().decode(response.getBytes1());

                    if (Arrays.equals(randomSent, randomBack)) // sub-string to cut block padding
                    {
                        System.out.println("Authentication Successful!");
                        sr = new SecureRandom();
                        counter = 0;
                        return true;
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                System.out.println("You must have your private key and the group servers public key in MyKeys");
            }
        } else if (response.getMessage().equals("NOKEY")) {
            System.out.println("FAILED! Group Server does not have your public key");
        }

        return false;
    }

    public synchronized UserToken getToken(String username, String wantedIP, String wantedPort) {
        try {
            UserToken token = null;
            Envelope message = null, response = null;

            //Tell the server to return a token.
            message = new Envelope("GET");
            message.addObject(username); //Add user name string
            message.addObject(wantedIP); //Add user name string
            message.addObject(wantedPort); //Add user name string
            encrypt(message, output);

            //Get the response from the server
            response = (Envelope) decrypt(input);

            //Successful response
            if (response.getMessage().equals("OK")) {
                //If there is a token in the Envelope, return it 
                ArrayList<Object> temp = null;
                temp = response.getObjContents();

                if (temp.size() == 1) {
                    token = (UserToken) temp.get(0);
                    return token;
                }
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
            return null;
        }

    }

    // used to get the required keychains a user will need to upload/download files
    @SuppressWarnings("unchecked")
    public synchronized void setKeyChainTables(String username, UserToken token) {
        try {
            Envelope message = null, response = null;

            //Tell the server to return a token.
            message = new Envelope("KCHAIN");
            message.addObject(username); //Add user name string
            message.addObject(token); //Add users token
            encrypt(message, output);

            //Get the response from the server
            response = (Envelope) decrypt(input);

            //Successful response
            if (response.getMessage().equals("OK")) {
                // we got a hashmap containing all keychains for all groups we are in
                // they only have keys for blocks the user is allowed to access
                encKeyChainTable = (Hashtable<String, ArrayList<String>>) response.getObjContents().get(0);
				intKeyChainTable = (Hashtable<String, ArrayList<String>>) response.getObjContents().get(0);
				System.out.println("encr kchain's size is: " + encKeyChainTable.size());
				System.out.println("int  kchain's size is: " + encKeyChainTable.size());
				return;
            } else if (response.getMessage().equals("MODIFIED")) {
                System.out.println("INVALID! Token tampering detected! Get a correct token and reconnect.");
            }
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
        }

    }

    public boolean createUser(String username, UserToken token) {
        try {
            Envelope message = null, response = null;
            //Tell the server to create a user
            message = new Envelope("CUSER");
            message.addObject(username); //Add user name string
            message.addObject(token); //Add the requester's token
            encrypt(message, output);

            response = (Envelope) decrypt(input);

            //If server indicates success, return true
            if (response.getMessage().equals("OK")) {
                return true;
            } else if (response.getMessage().equals("MODIFIED")) {
                System.out.println("INVALID! Token tampering detected! Get a correct token and reconnect.");
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
            return false;
        }
    }

    public boolean deleteUser(String username, UserToken token) {
        try {
            Envelope message = null, response = null;

            //Tell the server to delete a user
            message = new Envelope("DUSER");
            message.addObject(username); //Add user name
            message.addObject(token);  //Add requester's token
            encrypt(message, output);

            response = (Envelope) decrypt(input);

            //If server indicates success, return true
            if (response.getMessage().equals("OK")) {
                return true;
            } else if (response.getMessage().equals("MODIFIED")) {
                System.out.println("INVALID! Token tampering detected! Get a correct token and reconnect.");
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
            return false;
        }
    }

    public boolean createGroup(String groupname, UserToken token) {
        try {
            Envelope message = null, response = null;
            //Tell the server to create a group
            message = new Envelope("CGROUP");
            message.addObject(groupname); //Add the group name string
            message.addObject(token); //Add the requester's token
            encrypt(message, output);
            output.flush();

            response = (Envelope) decrypt(input);

            //If server indicates success, return true
            if (response.getMessage().equals("OK")) {
                return true;
            } else if (response.getMessage().equals("MODIFIED")) {
                System.out.println("INVALID! Token tampering detected! Get a correct token and reconnect.");
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
            return false;
        }
    }

    public boolean deleteGroup(String groupname, UserToken token) {
        try {
            Envelope message = null, response = null;
            //Tell the server to delete a group
            message = new Envelope("DGROUP");
            message.addObject(groupname); //Add group name string
            message.addObject(token); //Add requester's token
            encrypt(message, output);

            response = (Envelope) decrypt(input);

            //If server indicates success, return true
            if (response.getMessage().equals("OK")) {
                return true;
            } else if (response.getMessage().equals("MODIFIED")) {
                System.out.println("INVALID! Token tampering detected! Get a correct token and reconnect.");
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> listMembers(String group, UserToken token) {
        try {
            Envelope message = null, response = null;
            //Tell the server to return the member list
            message = new Envelope("LMEMBERS");
            message.addObject(group); //Add group name string
            message.addObject(token); //Add requester's token
            encrypt(message, output);

            response = (Envelope) decrypt(input);

            //If server indicates success, return the member list
            if (response.getMessage().equals("OK")) {
                return (List<String>) response.getObjContents().get(0); //This cast creates compiler warnings. Sorry.
            } else if (response.getMessage().equals("MODIFIED")) {
                System.out.println("INVALID! Token tampering detected! Get a correct token and reconnect.");
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
            return null;
        }
    }

    public boolean addUserToGroup(String username, String groupname, UserToken token) {
        try {
            Envelope message = null, response = null;
            //Tell the server to add a user to the group
            message = new Envelope("AUSERTOGROUP");
            message.addObject(username); //Add user name string
            message.addObject(groupname); //Add group name string
            message.addObject(token); //Add requester's token
            encrypt(message, output);

            response = (Envelope) decrypt(input);
            System.out.println("The response message:" + response.getMessage());
            //If server indicates success, return true
            if (response.getMessage().equals("OK")) {
                return true;
            } else if (response.getMessage().equals("MODIFIED")) {
                System.out.println("INVALID! Token tampering detected! Get a correct token and reconnect.");
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
            return false;
        }
    }

    public boolean deleteUserFromGroup(String username, String groupname, UserToken token) {
        try {
            Envelope message = null, response = null;
            //Tell the server to remove a user from the group
            message = new Envelope("RUSERFROMGROUP");
            message.addObject(username); //Add user name string
            message.addObject(groupname); //Add group name string
            message.addObject(token); //Add requester's token
            encrypt(message, output);

            response = (Envelope) decrypt(input);

            //If server indicates success, return true
            if (response.getMessage().equals("OK")) {
                return true;
            } else if (response.getMessage().equals("MODIFIED")) {
                System.out.println("INVALID! Token tampering detected! Get a correct token and reconnect.");
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
            return false;
        }
    }

    private void encrypt(Envelope Object, ObjectOutputStream output) {
        try {
            ByteArrayOutputStream boutput = new ByteArrayOutputStream();
            ObjectOutputStream oStream = new ObjectOutputStream(boutput);
            Object.addCounter(counter);
            counter++;
            oStream.writeObject(Object);
            byte[] byteValue = boutput.toByteArray();
            oStream.close();

            byte[] IV = new byte[16];
            sr.nextBytes(IV);
            IvParameterSpec spec = new IvParameterSpec(IV);
            Cipher aes = Cipher.getInstance("AES/CBC/PKCS7PADDING", new BouncyCastleProvider());
            aes.init(Cipher.ENCRYPT_MODE, sessionConfidentialityAESKey, spec);
            Envelope temp = new Envelope("Encrypted");
            Mac mac = Mac.getInstance("HmacSHA256", new BouncyCastleProvider());
            mac.init(sessionIntegrityAESKey);
            mac.update(byteValue);
            temp.addObject(new String(Base64.getEncoder().encode(IV)));
            temp.addBytes1(Base64.getEncoder().encode(mac.doFinal()));//put the HMAC here.
            temp.addBytes2(Base64.getEncoder().encode(aes.doFinal(byteValue)));
            output.writeObject(temp);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Object decrypt(ObjectInputStream input) {
        try {
            Envelope env = (Envelope) input.readObject();
            IvParameterSpec spec = new IvParameterSpec(Base64.getDecoder().decode(((String) env.getObjContents().get(0)).getBytes()));
            Cipher aes = Cipher.getInstance("AES/CBC/PKCS7PADDING", new BouncyCastleProvider());
            aes.init(Cipher.DECRYPT_MODE, sessionConfidentialityAESKey, spec);
            byte[] temp = aes.doFinal(Base64.getDecoder().decode(env.getBytes2()));
            byte[] passedHMAC = Base64.getDecoder().decode(env.getBytes1());

            ByteArrayInputStream in = new ByteArrayInputStream(temp);
            ObjectInputStream is = new ObjectInputStream(in);
            env = (Envelope) is.readObject();
            int counter1 = env.getCounter();
            if (counter1 != counter) {
                System.out.println("Error: detected tampering in the message received, counter didn't match.");
                sock.close();
                System.exit(0);
            }
            counter++;
            Mac mac = Mac.getInstance("HmacSHA256", new BouncyCastleProvider());
            mac.init(sessionIntegrityAESKey);
            //calculate the string to be used to calculate this HMAC, then compare.
            mac.update(temp);
            byte[] calculatedHMAC = mac.doFinal();
            if (!Arrays.equals(passedHMAC, calculatedHMAC)) {
                System.out.println("Error: detected tampering in the message received, HMAC didn't match.");
                sock.close();
                System.exit(0);
            }
            return env;
        } catch (IOException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(GroupClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(GroupClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(GroupClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(GroupClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
