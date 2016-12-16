/* File worker thread handles the business of uploading, downloading, and removing files for clients with valid tokens */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.Thread;
import java.net.Socket;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class FileThread extends Thread {

    private final Socket socket;
    DataLogger dl = new DataLogger("File");
    private SecretKey sessionConfidentialityAESKey = null;
    private SecretKey sessionIntegrityAESKey = null;
    private SecureRandom sr = null;
    private int counter;

    public FileThread(Socket _socket) {
        socket = _socket;
    }

    public void run() {
        boolean proceed = true;

        try {
            System.out.println("*** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + "***");
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            Envelope response = null;
            dl.write("data", "Connection from " + socket.getInetAddress() + ":" + socket.getPort() + " has been established.", "");
            File sysFile = new File("SystemKeys");
            if (!sysFile.exists()) {
                System.out.println("SystemKeys file does not exist.");
                System.exit(0);
            }
            do {
                if (sr == null) {
                    Envelope e = null;
                    try {
                        e = (Envelope) input.readObject();
                        System.out.println("Request received: " + e.getMessage());
                        dl.write("data", "Request received: " + e.getMessage(), "");
                    } catch (Exception ex) {
                        System.out.println("Error: A user abruptly terminated a session.");
                    }

                    if (e != null) {
                        if (e.getMessage().equals("AESAndChallenge")) {
                            BouncyCastleProvider p = new BouncyCastleProvider();
                            //get the private key from the MyKeys folder
                            PemReader daPrivPemReader = new PemReader(new InputStreamReader(new FileInputStream("SystemKeys/fs-privK.pem")));
                            PemObject daPrivPemObj = daPrivPemReader.readPemObject();
                            byte[] privateBytes = daPrivPemObj.getContent();
                            PKCS8EncodedKeySpec privEncoded = new PKCS8EncodedKeySpec(privateBytes);
                            KeyFactory daFactory = KeyFactory.getInstance("RSA", p);
                            PrivateKey serverPrivK = daFactory.generatePrivate(privEncoded);

                            daPrivPemReader.close();

                            //now we have recovered the private key for this server.  Use it to get the AES key and the random challenge to send back to the user.
                            String enc = (String) e.getObjContents().get(0);

                            Cipher rsaDec = Cipher.getInstance("RSA", p);
                            rsaDec.init(Cipher.DECRYPT_MODE, serverPrivK);
                            byte[] dec = rsaDec.doFinal(Base64.getDecoder().decode(enc.getBytes()));
                            byte[] aeskey = new byte[32];
                            byte[] aeskey1 = new byte[32];
                            byte[] chal = new byte[20];
                            System.arraycopy(dec, 0, aeskey, 0, aeskey.length);
                            System.arraycopy(dec, aeskey.length, aeskey1, 0, aeskey1.length);
                            System.arraycopy(dec, aeskey.length + aeskey1.length, chal, 0, chal.length);

                            SecretKey sharedAES = new SecretKeySpec(aeskey, 0, aeskey.length, "AES");
                            sessionConfidentialityAESKey = sharedAES;
                            SecretKey sharedAES1 = new SecretKeySpec(aeskey1, 0, aeskey1.length, "AES");
                            sessionIntegrityAESKey = sharedAES1;

                            response = new Envelope("ChallengeAndEncryptedChallenge");
                            response.addObject(new String(Base64.getEncoder().encode(chal)));

                            byte[] chal1 = new byte[20];
                            sr = new SecureRandom();
                            sr.nextBytes(chal1);
                            byte[] IV = new byte[16];
                            sr.nextBytes(IV);
                            Cipher aes = Cipher.getInstance("AES/CBC/PKCS7PADDING", new BouncyCastleProvider());
                            IvParameterSpec spec = new IvParameterSpec(IV);
                            aes.init(Cipher.ENCRYPT_MODE, sessionConfidentialityAESKey, spec);
                            response.addBytes2(Base64.getEncoder().encode(IV));
                            response.addBytes1(Base64.getEncoder().encode(aes.doFinal(chal1)));

                            output.writeObject(response);

                            response = (Envelope) input.readObject();
                            if (response.getMessage().equals("RandomChallenge")) {
                                if (!Arrays.equals(chal1, Base64.getDecoder().decode(response.getBytes1()))) {
                                    System.out.println("Error: the random challenge received from client does not match.");
                                    socket.close();
                                    System.exit(0);
                                }
                            }

                            counter = 0;
                        }
                    }
                } else {
                    Envelope e;
                    try {
                        e = (Envelope) decrypt(input);
                        System.out.println("Request received: " + e.getMessage());
                        dl.write("data", "Request received: " + e.getMessage(), "");
                    } catch (Exception ex2) {
                        e = null;
                    }
                    if (e != null) {
                        // Handler to list files that this user is allowed to see
                        if (e.getMessage().equals("LFILES")) {
                            if (e.getObjContents().size() <= 0) {
                                response = new Envelope("FAIL-BADCONTENTS");
                                dl.write("err", "LFILES FAIL-BADCONTENTS", "");
                            } else {
                                if (e.getObjContents().get(0) == null) {
                                    response = new Envelope("FAIL-BADTOKEN");
                                    dl.write("err", "LFILES FAIL-BADTOKEN", "");
                                } else {
                                    //check that the token wasn't tampered with.
                                    ArrayList<Object> envelopeContents = e.getObjContents();
                                    Token token = (Token) envelopeContents.get(0);

                                    byte[] newHashedToken = computeTokenHash(token.getIssuer(), token.getSubject(), token.getGroups(), token.getIPAddress(), token.getPortNumber());
                                    if (verifySignature(newHashedToken, token.getSignedHash(), token)) {
                                        ArrayList<ShareFile> fileList = FileServer.fileList.getFiles();
                                        ArrayList<String> userFiles = new ArrayList<>();
                                        List<String> groups = token.getGroups();

                                        if (fileList != null) {//i.e. there is something in the file list.
                                            for (int i = 0; i < fileList.size(); i++) {
                                                ShareFile share = fileList.get(i);

                                                if (groups.contains(share.getGroup())) {
                                                    userFiles.add("File: " + share.getPath() + " File Owner:" + share.getOwner());
                                                }
                                            }
                                        }

                                        response = new Envelope("OK");
                                        response.addObject(userFiles);
                                        dl.write("data", "List files successfully completed.", "");
                                    } else {
                                        response = new Envelope("FAIL-BADTOKEN");
                                        dl.write("err", "LFILES FAIL-BADTOKEN", "");
                                    }
                                }
                            }
                            encrypt(response, output);
                        }
                        if (e.getMessage().equals("UPLOADF")) {

                            if (e.getObjContents().size() < 3) {
                                response = new Envelope("FAIL-BADCONTENTS");
                                dl.write("err", "UPLOADF FAIL-BADCONTENTS", "");
                            } else {
                                if (e.getObjContents().get(0) == null) {
                                    response = new Envelope("FAIL-BADPATH");
                                    dl.write("err", "UPLOADF FAIL-BADPATH", "");
                                }
                                if (e.getObjContents().get(1) == null) {
                                    response = new Envelope("FAIL-BADGROUP");
                                    dl.write("err", "UPLOADF FAIL-BADGROUP", "");
                                }
                                if (e.getObjContents().get(2) == null) {
                                    response = new Envelope("FAIL-BADTOKEN");
                                    dl.write("err", "UPLOADF FAIL-BADTOKEN", "");
                                } else {
                                    String remotePath = (String) e.getObjContents().get(0);
                                    String group = (String) e.getObjContents().get(1);
                                    Token yourToken = (Token) e.getObjContents().get(2); //Extract token
                                    File checkfile = new File("shared_files/" + remotePath);

                                    byte[] newHashedToken = computeTokenHash(yourToken.getIssuer(), yourToken.getSubject(), yourToken.getGroups(), yourToken.getIPAddress(), yourToken.getPortNumber());
                                    if (checkfile.exists() && !checkfile.isDirectory()) {
                                        System.out.printf("Error: File already exists at %s\n", remotePath);
                                        response = new Envelope("FAIL-FILEEXISTS"); //Success
                                        dl.write("err", "UPLOADF FAIL-FILEEXISTS", "");
                                    } else if (!yourToken.getGroups().contains(group)) {
                                        System.out.printf("Error: User missing valid token for group %s\n", group);
                                        response = new Envelope("FAIL-UNAUTHORIZED"); //Success
                                        dl.write("err", "UPLOADF FAIL-UNAUTHORIZED", "");
                                    } else if (!verifySignature(newHashedToken, yourToken.getSignedHash(), yourToken)) {
                                        System.out.println("Error: Token has been tampered with.");
                                        response = new Envelope("FAIL-BADTOKEN");
                                        dl.write("err", "UPLOADF FAIL-BADTOKEN", "");
                                    } else {
                                        remotePath = remotePath.replace("/", "");
                                        File file = new File("shared_files/" + remotePath);
                                        file.createNewFile();
                                        FileOutputStream fos = new FileOutputStream(file);
                                        System.out.printf("Successfully created file %s\n", remotePath);

                                        response = new Envelope("READY"); //Success
                                        encrypt(response, output);
                                        dl.write("data", "Successfully created file " + remotePath, "");

                                        // try to read, it can cause error if connection reset/client abruptly terminates
                                        try {
                                            e = (Envelope) decrypt(input);
                                        } catch (Exception ex) {
                                            System.out.println("Error: Connection was reset, unable to read incoming data.");
                                            dl.write("err", "Connection was reset, unable to read incoming data.", "");
                                        }
                                        ArrayList<String> IVList = new ArrayList<>();

                                        while (e.getMessage().compareTo("CHUNK") == 0) {
                                            fos.write((byte[]) e.getObjContents().get(0), 0, (Integer) e.getObjContents().get(1));
                                            IVList.add((String) e.getObjContents().get(2));
                                            response = new Envelope("READY"); //Successl
                                            encrypt(response, output);
                                            e = (Envelope) decrypt(input);
                                        }

                                        if (e.getMessage().compareTo("EOF") == 0) {
                                            System.out.printf("Transfer successful file %s\n", remotePath);
                                            FileServer.fileList.addFile(yourToken.getSubject(), group, remotePath, (Integer) e.getObjContents().get(0), IVList, (String) e.getObjContents().get(1));
                                            response = new Envelope("OK"); //Success
                                        } else {
                                            System.out.printf("Error reading file %s from client\n", remotePath);
                                            response = new Envelope("ERROR-TRANSFER"); //Success
                                            dl.write("err", "UPLOADF ERROR-TRANSFER", "");
                                        }
                                        fos.close();
                                    }
                                }
                            }

                            encrypt(response, output);
                        } else if (e.getMessage().compareTo("DOWNLOADF") == 0) {

                            String remotePath = (String) e.getObjContents().get(0);
                            Token t = (Token) e.getObjContents().get(1);
                            ShareFile sf = FileServer.fileList.getFile(remotePath);

                            ArrayList<String> IVList = sf.getIVList();
                            byte[] newHashedToken = computeTokenHash(t.getIssuer(), t.getSubject(), t.getGroups(), t.getIPAddress(), t.getPortNumber());
                            if (sf == null) {
                                System.out.printf("Error: File %s doesn't exist\n", remotePath);
                                e = new Envelope("ERROR_FILEMISSING");
                                encrypt(e, output);
                                dl.write("err", "DOWNLOADF ERROR_FILEMISSING", "");

                            } else if (!t.getGroups().contains(sf.getGroup())) {
                                System.out.printf("Error user %s doesn't have permission\n", t.getSubject());
                                e = new Envelope("ERROR_PERMISSION");
                                encrypt(e, output);
                                dl.write("err", "DOWNLOADF ERROR_PERMISSION", "");
                            } else if (!verifySignature(newHashedToken, t.getSignedHash(), t)) {
                                System.out.println("Error: Token has been tampered with.");
                                response = new Envelope("FAIL-BADTOKEN");
                                dl.write("err", "DOWNLOADF FAIL-BADTOKEN", "");
                            } else {

                                try {
                                    File f = new File("shared_files/" + remotePath);
                                    if (!f.exists()) {
                                        System.out.printf("Error file %s missing from disk\n", remotePath);
                                        e = new Envelope("ERROR_NOTONDISK");
                                        encrypt(e, output);
                                        dl.write("err", "DOWNLOADF ERROR_NOTONDISK", "");

                                    } else {
                                        FileInputStream fis = new FileInputStream(f);
                                        int x = 0;

                                        do {
                                            String IV = IVList.get(x);
                                            x++;
                                            byte[] buf = new byte[4096];
                                            if (e.getMessage().compareTo("DOWNLOADF") != 0) {
                                                System.out.printf("Server error: %s\n", e.getMessage());
                                                dl.write("err", "DOWNLOADF Server error: " + e.getMessage(), "");
                                                break;
                                            }
                                            e = new Envelope("CHUNK");
                                            int n = fis.read(buf); //can throw an IOException
                                            if (n > 0) {
                                                System.out.printf(".");
                                            } else if (n < 0) {
                                                System.out.println("Read error");
                                                dl.write("err", "DOWNLOADF Read error", "");

                                            }

                                            e.addObject(buf);
                                            e.addObject(new Integer(n));
                                            e.addObject(sf.getGroup());
                                            e.addObject(new Integer(sf.getKeyNumber()));
                                            e.addObject(IV);

                                            encrypt(e, output);

                                            e = (Envelope) decrypt(input);

                                        } while (fis.available() > 0);
                                        fis.close();

                                        //If server indicates success, return the member list
                                        if (e.getMessage().compareTo("DOWNLOADF") == 0) {

                                            e = new Envelope("EOF");
                                            e.addObject(sf.getHMAC());
                                            encrypt(e, output);

                                            e = (Envelope) decrypt(input);
                                            if (e.getMessage().compareTo("OK") == 0) {
                                                System.out.printf("File data upload successful\n");
                                                dl.write("data", "File data upload successful", "");
                                            } else {

                                                System.out.printf("Upload failed: %s\n", e.getMessage());
                                                dl.write("err", "DOWNLOADF Upload failed: " + e.getMessage(), "");

                                            }

                                        } else {

                                            System.out.printf("Upload failed: %s\n", e.getMessage());
                                            dl.write("err", "DOWNLOADF Upload failed: " + e.getMessage(), "");

                                        }
                                    }
                                } catch (Exception e1) {
                                    System.err.println("Error: " + e.getMessage());
                                    e1.printStackTrace(System.err);
                                    dl.write("err", "Error: " + e.getMessage(), "");

                                }
                            }
                        } else if (e.getMessage().compareTo("DELETEF") == 0) {

                            String remotePath = (String) e.getObjContents().get(0);
                            Token t = (Token) e.getObjContents().get(1);
                            ShareFile sf = FileServer.fileList.getFile(remotePath);
                            byte[] newHashedToken = computeTokenHash(t.getIssuer(), t.getSubject(), t.getGroups(), t.getIPAddress(), t.getPortNumber());
                            if (sf == null) {
                                System.out.printf("Error: File %s doesn't exist\n", remotePath);
                                e = new Envelope("ERROR_DOESNTEXIST");
                                dl.write("err", "DELETEF ERROR_DOESNTEXIST", "");
                            } else if (!t.getGroups().contains(sf.getGroup())) {
                                System.out.printf("Error user %s doesn't have permission\n", t.getSubject());
                                e = new Envelope("ERROR_PERMISSION");
                                dl.write("err", "DELETEF ERROR_PERMISSION", "");
                            } else if (!verifySignature(newHashedToken, t.getSignedHash(), t)) {
                                System.out.println("Error: Token has been tampered with.");
                                response = new Envelope("FAIL-BADTOKEN");
                                dl.write("err", "DELETEF FAIL-BADTOKEN", "");
                            } else {

                                try {

                                    File f = new File("shared_files/" + remotePath);

                                    if (!f.exists()) {
                                        System.out.printf("Error file %s missing from disk\n", remotePath);
                                        e = new Envelope("ERROR_FILEMISSING");
                                        dl.write("err", "DELETEF ERROR_FILEMISSING", "");
                                    } else if (f.delete()) {
                                        System.out.printf("File %s deleted from disk\n", remotePath);
                                        FileServer.fileList.removeFile(remotePath);
                                        e = new Envelope("OK");
                                        dl.write("data", "File " + remotePath + " was successfully deleted from disk", "");
                                    } else {
                                        System.out.printf("Error deleting file %s from disk\n", remotePath);
                                        e = new Envelope("ERROR_DELETE");
                                        dl.write("err", "DELETEF ERROR_DELETE", "");
                                    }

                                } catch (Exception e1) {
                                    System.err.println("Error: " + e1.getMessage());
                                    e1.printStackTrace(System.err);
                                    e = new Envelope(e1.getMessage());
                                    dl.write("err", "Error: " + e1.getMessage(), "");
                                }
                            }
                            encrypt(e, output);

                        } else if (e.getMessage().equals("DISCONNECT")) {
                            socket.close();
                            proceed = false;
                            dl.write("data", "connection disconnected", "");

                            sessionConfidentialityAESKey = null;
                        }
                    } else {
                        proceed = false;
                    }
                }
            } while (proceed);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            dl.write("err", "Error: " + e.getMessage(), "");
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
                socket.close();
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
                socket.close();
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

    private boolean verifySignature(byte[] computedHash, byte[] signedHash, Token token) {
        byte[] returnHash = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            PemReader daPubPemReader = new PemReader(new InputStreamReader(new FileInputStream("SystemKeys/gs-pubK.pem")));
            PemObject daPubPemObj = daPubPemReader.readPemObject();
            byte[] publicBytes = daPubPemObj.getContent();
            X509EncodedKeySpec pubEncoded = new X509EncodedKeySpec(publicBytes);
            KeyFactory daFactory = KeyFactory.getInstance("RSA");
            PublicKey pubK = daFactory.generatePublic(pubEncoded);

            daPubPemReader.close();

            Signature signature = Signature.getInstance("SHA256withRSA", "BC");
            signature.initVerify(pubK);
            signature.update(computedHash);

            String ourIPAddr = "" + Inet4Address.getLocalHost().getHostAddress();
            String ourPort = "" + socket.getLocalPort();
            System.out.println("Ours: " + ourIPAddr);
            System.out.println("Ours: " + ourPort);
            System.out.println("Theirs: " + token.getIPAddress());
            System.out.println("Theirs: " + token.getPortNumber());

            // the signature on the token was verified!
            // check the token's ip and port and ensure intended for here
            if (signature.verify(signedHash)) {
                // the signature on the token was verified!
                // check the token's ip and port and ensure intended for here
                if (token.getIPAddress().equals(ourIPAddr) && token.getPortNumber().equals(ourPort)) {
                    return true;
                } else {
                    return false;
                }

            } else {
                return false;
            }

        } catch (NoSuchAlgorithmException ex) {
            System.out.println("alg");
        } catch (NoSuchProviderException ex) {
            System.out.println("nosuchprovider");
        } catch (InvalidKeyException ex) {
            System.out.println("invalid key");
        } catch (SignatureException ex) {
            System.out.println("signature exception");
        } catch (IOException ex) {
            System.out.println("ioexception");
        } catch (InvalidKeySpecException ex) {
            System.out.println("invalid key spec");
        }
        return false;
    }

    private byte[] computeTokenHash(String server, String username, List<String> groups, String ipFound, String portFound) {
        try {
            byte[] hashedToken = null;
            ArrayList<String> userGroups = (ArrayList<String>) groups;
            Collections.sort(userGroups);
            String toBeHashed = server + "\0" + username + "\0" + ipFound + "\0" + portFound + "\0";
            for (int i = 0; i < userGroups.size(); i++) {
                toBeHashed = toBeHashed + userGroups.get(i) + "\0";
            }
            MessageDigest theDigester = MessageDigest.getInstance("SHA-256");
            theDigester.update(toBeHashed.getBytes());
            hashedToken = theDigester.digest();
            return hashedToken;
        } catch (Exception e) {
            return null;
        }
    }

}
