/* This thread does all the work. It communicates with the client through Envelopes.
 * 
 */

import java.lang.Thread;
import java.net.Socket;
import java.io.*;
import java.util.*;

// include crypto librarys needed
import javax.crypto.*;
import javax.crypto.KeyGenerator;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.spec.IvParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

// add pemobject and pemreader for parsing public/private keys
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

public class GroupThread extends Thread {

    DataLogger gLog = new DataLogger("Group");
    StringWriter stackTraceString = new StringWriter();
    PrintWriter stackTrace = new PrintWriter(stackTraceString);

    private final Socket socket;
    private GroupServer my_gs;
    private SecretKey sessionConfidentialityAESKey;
    private SecretKey sessionIntegrityAESKey;
    private byte[] authenticationHash;
    private SecureRandom sr = null;
    private int counter;

    public GroupThread(Socket _socket, GroupServer _gs) {
        socket = _socket;
        my_gs = _gs;
    }

    public void run() {
        Security.addProvider(new BouncyCastleProvider());
        boolean proceed = true;

        try {
            //Announces connection and opens object streams
            System.out.println("*** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + "***");
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            do {
                if (sr == null) {
                    Envelope message = (Envelope) input.readObject();
                    System.out.println("Request received: " + message.getMessage());
                    Envelope response;
                    if (message.getMessage().equals("AUTH1")) {
                        // open the users public key from the UserPubKeys folder
                        String username = (String) message.getObjContents().get(0); //Get the username
                        // if the user isnt in the system dont even try
                        if (username != null && my_gs.userList.checkUser(username)) {
                            try {
                                // their username exist
                                // try to open their public key for encrypting session key
                                PemReader userPubPemReader = new PemReader(new InputStreamReader(new FileInputStream("UserPubKeys/user-" + username + "-pubK.pem")));
                                PemObject userPubPemObj = userPubPemReader.readPemObject();
                                byte[] publicBytes = userPubPemObj.getContent();
                                X509EncodedKeySpec pubEncoded = new X509EncodedKeySpec(publicBytes);
                                KeyFactory daFactory = KeyFactory.getInstance("RSA");
                                PublicKey userPubK = daFactory.generatePublic(pubEncoded);

                                userPubPemReader.close();

                                // create the shared key to be used in that session
                                KeyGenerator theGenerator = KeyGenerator.getInstance("AES", "BC");
                                theGenerator.init(256, new SecureRandom());
                                sessionConfidentialityAESKey = theGenerator.generateKey();
                                theGenerator.init(256, new SecureRandom());
                                sessionIntegrityAESKey = theGenerator.generateKey();

                                // generate a fresh random challenge
                                SecureRandom rand = new SecureRandom();
                                byte[] challenge = new byte[20];//160 bit random challenge.
                                rand.nextBytes(challenge);

                                // generate the hash of the session key bytes and challenge byte to temporarily remember
                                byte[] concat = new byte[84];
                                byte[] encodedSesAes = sessionConfidentialityAESKey.getEncoded();
                                byte[] encodedIntegrity = sessionIntegrityAESKey.getEncoded();
                                System.arraycopy(encodedSesAes, 0, concat, 0, encodedSesAes.length);//copying the encoded key, and the challenge into a concat byte array
                                System.arraycopy(encodedIntegrity, 0, concat, encodedSesAes.length, encodedIntegrity.length);
                                System.arraycopy(challenge, 0, concat, (encodedSesAes.length + encodedIntegrity.length), challenge.length);
                                MessageDigest theDigester = MessageDigest.getInstance("SHA-256", "BC");
                                theDigester.update(concat);
                                authenticationHash = theDigester.digest();

                                // encrypt the key and send over the bytes
                                Cipher cipher = Cipher.getInstance("RSA", "BC");
                                cipher.init(Cipher.ENCRYPT_MODE, userPubK);
                                byte[] encryptedConf = cipher.doFinal(encodedSesAes);
                                encryptedConf = Base64.getEncoder().encode(encryptedConf);
                                byte[] encryptedInteg = cipher.doFinal(encodedIntegrity);
                                encryptedInteg = Base64.getEncoder().encode(encryptedInteg);

                                // everything went fine, send challenge and encrypted session key
                                response = new Envelope("1");
                                response.addBytes1(Base64.getEncoder().encode(challenge));
                                response.addBytes2(encryptedConf);
                                response.addBytes3(encryptedInteg);
                                output.writeObject(response);
                            } catch (Exception ex) {
                                // if their public key file is not on the server, fail
                                response = new Envelope("NOKEY");
                                response.addBytes1(null);
                                output.writeObject(response);

                                System.out.println(ex.getMessage());
                                ex.printStackTrace();
                            }
                        } else {
                            response = new Envelope("FAIL");
                            response.addBytes1(null);
                            output.writeObject(response);
                        }
                    } else if (message.getMessage().equals("AUTH2")) {
                        byte[] hashToCheck = Base64.getDecoder().decode(message.getBytes1());
                        byte[] encryptedChallenge = Base64.getDecoder().decode(message.getBytes2());
                        // if hashes are the same, then we know its them
                        //System.out.println("H: "+new String(hashToCheck));
                        //System.out.println("A: "+new String(authenticationHash));
                        if (Arrays.equals(authenticationHash, hashToCheck)) {
                            try {
                                // hashes were the same, decrypt the challenge and send it back to them
                                PemReader sysPrivPemReader = new PemReader(new InputStreamReader(new FileInputStream("SystemKeys/gs-privK.pem")));
                                PemObject sysPrivPemObj = sysPrivPemReader.readPemObject();
                                byte[] privateBytes = sysPrivPemObj.getContent();
                                PKCS8EncodedKeySpec privEncoded = new PKCS8EncodedKeySpec(privateBytes);
                                KeyFactory daFactory = KeyFactory.getInstance("RSA");
                                PrivateKey privK = daFactory.generatePrivate(privEncoded);

                                sysPrivPemReader.close();

                                Cipher cipher = Cipher.getInstance("RSA", "BC");
                                cipher.init(Cipher.DECRYPT_MODE, privK);
                                // set-up space for the decrypted bytes to go
                                byte[] decryptedChallenge = cipher.doFinal(encryptedChallenge);
                                //decryptedChallenge = new byte[cipher.getOutputSize(encryptedChallenge.length)];
                                //int decryptedLength = cipher.update(encryptedChallenge, 0, encryptedChallenge.length, decryptedChallenge, 0);
                                //decryptedLength += cipher.doFinal(decryptedChallenge, decryptedLength);

                                // now we have the decryptedChallenge bytes, send them back
                                System.out.println("Mutual authentication successful.");
                                message = new Envelope("2");
                                message.addBytes1(Base64.getEncoder().encode(decryptedChallenge));
                                output.writeObject(message);
                                sr = new SecureRandom();
                                counter = 0;
                            } catch (Exception ex) {
                                response = new Envelope("FAIL");
                                response.addBytes1(null);
                                output.writeObject(response);
                            }
                        } else {
                            response = new Envelope("FAIL");
                            response.addBytes1(null);
                            output.writeObject(response);
                        }
                    }
                } else {
                    Envelope message = null;
                    Envelope response;
                    try {
                        message = (Envelope) decrypt(input);
                        System.out.println(message.getMessage());
                    } catch (Exception e) {
                    }
                    if (message.getMessage().equals("GET"))//Client wants a token
                    {
                        String username = (String) message.getObjContents().get(0); //Get the username
                        String wantedIP = (String) message.getObjContents().get(1); //Get the wantedIP
                        String wantedPort = (String) message.getObjContents().get(2); //Get the wantedPort
                        if (username == null || wantedIP == null || wantedPort == null) {
                            response = new Envelope("FAIL");
                            response.addObject(null);
                            encrypt(response, output);
                        } else {
                            UserToken yourToken = createToken(username, wantedIP, wantedPort); //Create a token
                            System.out.println("Token issued to: " + username);
                            //Respond to the client. On error, the client will receive a null token
                            response = new Envelope("OK");
                            response.addObject(yourToken);
                            encrypt(response, output);
                        }
                    } else if (message.getMessage().equals("KCHAIN")) {
                        if (message.getObjContents().size() < 2) {
                            response = new Envelope("FAIL");
                        } else {
                            response = new Envelope("FAIL");

                            if (message.getObjContents().get(0) != null) {
                                if (message.getObjContents().get(1) != null) {
                                    String username = (String) message.getObjContents().get(0); //Extract the username
                                    UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract the token
                                    byte[] newHashedToken = computeTokenHash(yourToken.getIssuer(), yourToken.getSubject(), yourToken.getGroups(), yourToken.getIPAddress(), yourToken.getPortNumber());
                                    if (verifySignature(newHashedToken, yourToken.getSignedHash())) {
                                        System.out.println(yourToken.getSubject() + "'s token was successfully verified.");

                                        // username and token are valid, create their keychain and send it back
                                        Hashtable<String, ArrayList<String>> encKeyChainTable = createKeyChainTable(username, yourToken, "e");
										Hashtable<String, ArrayList<String>> intKeyChainTable = createKeyChainTable(username, yourToken, "i");
										System.out.println("encr kchain's size is: " + encKeyChainTable.size());
										System.out.println("int  kchain's size is: " + encKeyChainTable.size());
										if (encKeyChainTable != null && intKeyChainTable != null) {
											System.out.println("Valid encryption keychain and integrity keychain made");
                                            response = new Envelope("OK"); //Success
                                            response.addObject(encKeyChainTable);
											response.addObject(intKeyChainTable);
                                            System.out.println(yourToken.getSubject() + " was successfully given their keychains");
                                        }
                                    } else {
                                        response = new Envelope("MODIFIED");
                                    }
                                }
                            }
                        }

                        encrypt(response, output);
                    } else if (message.getMessage().equals("CUSER")) //Client wants to create a user
                    {
                        if (message.getObjContents().size() < 2) {
                            response = new Envelope("FAIL");
                        } else {
                            response = new Envelope("FAIL");

                            if (message.getObjContents().get(0) != null) {
                                if (message.getObjContents().get(1) != null) {
                                    String username = (String) message.getObjContents().get(0); //Extract the username
                                    UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract the token
                                    byte[] newHashedToken = computeTokenHash(yourToken.getIssuer(), yourToken.getSubject(), yourToken.getGroups(), yourToken.getIPAddress(), yourToken.getPortNumber());
                                    if (verifySignature(newHashedToken, yourToken.getSignedHash())) {
                                        System.out.println(yourToken.getSubject() + "'s token was successfully verified.");
                                        if (createUser(username, yourToken)) {
                                            response = new Envelope("OK"); //Success
                                            System.out.println("Admin " + yourToken.getSubject() + " successfully created User: " + username);
                                        }
                                    } else {
                                        response = new Envelope("MODIFIED");
                                    }
                                }
                            }
                        }

                        encrypt(response, output);
                    } else if (message.getMessage().equals("DUSER")) //Client wants to delete a user
                    {

                        if (message.getObjContents().size() < 2) {
                            response = new Envelope("FAIL");
                        } else {
                            response = new Envelope("FAIL");

                            if (message.getObjContents().get(0) != null) {
                                if (message.getObjContents().get(1) != null) {
                                    String username = (String) message.getObjContents().get(0); //Extract the username
                                    UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract the token

                                    byte[] newHashedToken = computeTokenHash(yourToken.getIssuer(), yourToken.getSubject(), yourToken.getGroups(), yourToken.getIPAddress(), yourToken.getPortNumber());
                                    if (verifySignature(newHashedToken, yourToken.getSignedHash())) {
                                        System.out.println(yourToken.getSubject() + "'s token was successfully verified.");
                                        if (deleteUser(username, yourToken)) {
                                            response = new Envelope("OK"); //Success
                                            System.out.println("Admin " + yourToken.getSubject() + " successfully deleted User: " + username);
                                        }
                                    } else {
                                        response = new Envelope("MODIFIED");
                                    }
                                }
                            }
                        }
                        encrypt(response, output);
                    } else if (message.getMessage().equals("CGROUP")) //Client wants to create a group
                    {
                        // first should be the group second should be the token
                        if (message.getObjContents().size() < 2) {
                            response = new Envelope("FAIL");
                        } else {
                            response = new Envelope("FAIL");

                            if (message.getObjContents().get(0) != null) {
                                if (message.getObjContents().get(1) != null) {
                                    String daGroupName = (String) message.getObjContents().get(0); //Extract the group to be created
                                    UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract the token

                                    byte[] newHashedToken = computeTokenHash(yourToken.getIssuer(), yourToken.getSubject(), yourToken.getGroups(), yourToken.getIPAddress(), yourToken.getPortNumber());
                                    if (verifySignature(newHashedToken, yourToken.getSignedHash())) {
                                        System.out.println(yourToken.getSubject() + "'s token was successfully verified.");
                                        // method implemented found below
                                        if (createGroup(daGroupName, yourToken)) {
                                            response = new Envelope("OK"); //Success
                                            System.out.println("User " + yourToken.getSubject() + " successfully created Group: " + daGroupName);
                                        }
                                    } else {
                                        response = new Envelope("MODIFIED");
                                    }
                                }
                            }
                        }
                        encrypt(response, output);
                    } else if (message.getMessage().equals("DGROUP")) //Client wants to delete a group
                    {
                        // first should be group second should be token
                        if (message.getObjContents().size() < 2) {
                            response = new Envelope("FAIL");
                        } else {
                            response = new Envelope("FAIL");

                            if (message.getObjContents().get(0) != null) {
                                if (message.getObjContents().get(1) != null) {
                                    String daGroupName = (String) message.getObjContents().get(0); //Extract group to be deleted
                                    UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract the token

                                    byte[] newHashedToken = computeTokenHash(yourToken.getIssuer(), yourToken.getSubject(), yourToken.getGroups(), yourToken.getIPAddress(), yourToken.getPortNumber());
                                    if (verifySignature(newHashedToken, yourToken.getSignedHash())) {
                                        System.out.println(yourToken.getSubject() + "'s token was successfully verified.");
                                        // method implementation found below
                                        if (deleteGroup(daGroupName, yourToken)) {
                                            response = new Envelope("OK"); //Success
                                            System.out.println("User " + yourToken.getSubject() + " successfully deleted Group: " + daGroupName);
                                        }
                                    } else {
                                        response = new Envelope("MODIFIED");
                                    }
                                }
                            }
                        }

                        encrypt(response, output);
                    } else if (message.getMessage().equals("LMEMBERS")) //Client wants a list of members in a group
                    {
                        // first should be group second should be token
                        if (message.getObjContents().size() < 2) {
                            response = new Envelope("FAIL");
                        } else {
                            response = new Envelope("FAIL");

                            if (message.getObjContents().get(0) != null) {
                                if (message.getObjContents().get(1) != null) {
                                    String daGroupName = (String) message.getObjContents().get(0); //Extract group whose members we list
                                    UserToken yourToken = (UserToken) message.getObjContents().get(1); //Extract the token

                                    byte[] newHashedToken = computeTokenHash(yourToken.getIssuer(), yourToken.getSubject(), yourToken.getGroups(), yourToken.getIPAddress(), yourToken.getPortNumber());
                                    if (verifySignature(newHashedToken, yourToken.getSignedHash())) {
                                        System.out.println(yourToken.getSubject() + "'s token was successfully verified.");
                                        ArrayList<String> listOfMembers = listMembersOfGroup(daGroupName, yourToken);
                                        if (listOfMembers != null) // we built a list
                                        {
                                            response = new Envelope("OK"); //Success
                                            response.addObject(listOfMembers);
                                        }
                                    } else {
                                        response = new Envelope("MODIFIED");
                                    }
                                    encrypt(response, output);
                                }
                            }
                        }
                    } else if (message.getMessage().equals("AUSERTOGROUP")) //Client wants to add user to a group
                    {
                        // first should be username, second should be group third should be token
                        if (message.getObjContents().size() < 3) {
                            response = new Envelope("FAIL");
                        } else {
                            response = new Envelope("FAIL");

                            if (message.getObjContents().get(0) != null) {
                                if (message.getObjContents().get(1) != null) {
                                    if (message.getObjContents().get(2) != null) {
                                        String daUserName = (String) message.getObjContents().get(0); //Extract the username
                                        String daGroupName = (String) message.getObjContents().get(1); //Extract the group
                                        UserToken yourToken = (UserToken) message.getObjContents().get(2); //Extract the token
									/* method found below */
                                        byte[] newHashedToken = computeTokenHash(yourToken.getIssuer(), yourToken.getSubject(), yourToken.getGroups(), yourToken.getIPAddress(), yourToken.getPortNumber());
                                        if (verifySignature(newHashedToken, yourToken.getSignedHash())) {
                                            System.out.println(yourToken.getSubject() + "'s token was successfully verified.");
                                            if (addUserToGroup(daUserName, daGroupName, yourToken)) {
                                                response = new Envelope("OK"); //Success
                                                System.out.println("User " + yourToken.getSubject() + " successfully added User: " + daUserName + " to Group: " + daGroupName);
                                            }
                                        } else {
                                            response = new Envelope("MODIFIED");
                                        }
                                    }
                                }
                            }
                        }
                        encrypt(response, output);
                    } else if (message.getMessage().equals("RUSERFROMGROUP")) //Client wants to remove user from a group
                    {
                        // same as adding users to groups, we need the same info
                        if (message.getObjContents().size() < 3) {
                            response = new Envelope("FAIL");
                        } else {
                            response = new Envelope("FAIL");

                            if (message.getObjContents().get(0) != null) {
                                if (message.getObjContents().get(1) != null) {
                                    if (message.getObjContents().get(2) != null) {
                                        String daUserName = (String) message.getObjContents().get(0); //Extract the username
                                        String daGroupName = (String) message.getObjContents().get(1); //Extract the group
                                        UserToken yourToken = (UserToken) message.getObjContents().get(2); //Extract the token

                                        byte[] newHashedToken = computeTokenHash(yourToken.getIssuer(), yourToken.getSubject(), yourToken.getGroups(), yourToken.getIPAddress(), yourToken.getPortNumber());
                                        if (verifySignature(newHashedToken, yourToken.getSignedHash())) {
                                            System.out.println(yourToken.getSubject() + "'s token was successfully verified.");
                                            if (deleteUserFromGroup(daUserName, daGroupName, yourToken)) {
                                                // they were successfully removed, generate a new group key
                                                int currentBlock = my_gs.keyList.getCurBlockNum(daGroupName);
                                                my_gs.keyList.addKeysToGroup(daGroupName);
                                                my_gs.userList.addUserEndBlock(daUserName, daGroupName, currentBlock);
                                                response = new Envelope("OK"); //Success
                                                System.out.println("User " + yourToken.getSubject() + " successfully removed User: " + daUserName + " from Group: " + daGroupName);
                                            }
                                        } else {
                                            response = new Envelope("MODIFIED");
                                        }
                                    }
                                }
                            }
                        }
                        encrypt(response, output);
                    } else if (message.getMessage().equals("DISCONNECT")) //Client wants to disconnect
                    {
                        sr = null;
                        socket.close(); //Close the socket
                        proceed = false; //End this communication loop
                    } else {
                        response = new Envelope("FAIL"); //Server does not understand client request
                        encrypt(response, output);
                    }
                }
            } while (proceed);
        } catch (Exception e) {
            e.printStackTrace(stackTrace);
            gLog.write("err", e.getMessage(), stackTraceString.toString());
        }
    }

    //Method to create tokens
    private UserToken createToken(String username, String wantedIP, String wantedPort) {
        //Check that user exists
        if (my_gs.userList.checkUser(username)) {
            // using BouncyCastleProvider
            Security.addProvider(new BouncyCastleProvider());
            try {
                // load the server's private key file to be used in signature process
                PemReader daPrivPemReader = new PemReader(new InputStreamReader(new FileInputStream("SystemKeys/gs-privK.pem")));
                PemObject daPrivPemObj = daPrivPemReader.readPemObject();
                byte[] privateBytes = daPrivPemObj.getContent();
                PKCS8EncodedKeySpec privEncoded = new PKCS8EncodedKeySpec(privateBytes);
                KeyFactory daFactory = KeyFactory.getInstance("RSA");
                PrivateKey privK = daFactory.generatePrivate(privEncoded);

                // create the concatenated string, add sentinel value for separation
                String toBeHashed = my_gs.name + "\0" + username + "\0" + wantedIP + "\0" + wantedPort + "\0";
                ArrayList<String> userGroups = my_gs.userList.getUserGroups(username);
                // sort the groups so they are hashed in a consistent manner
                Collections.sort(userGroups);
                for (int i = 0; i < userGroups.size(); i++) {
                    toBeHashed = toBeHashed + userGroups.get(i) + "\0";
                }
                // create the SHA256 hash of the concatenated string, sign it with privK
                MessageDigest theDigester = MessageDigest.getInstance("SHA-256", new BouncyCastleProvider());
                theDigester.update(toBeHashed.getBytes());
                byte[] hashedToken = theDigester.digest();
                Signature signature = Signature.getInstance("SHA256withRSA", "BC");
                signature.initSign(privK, new SecureRandom());
                signature.update(hashedToken);
                byte[] signedHash = signature.sign();

                //Issue a new token with server's name, user's name, and user's groups
                UserToken yourToken = new Token(my_gs.name, username, my_gs.userList.getUserGroups(username), wantedIP, wantedPort, signedHash);
                //log that the server issued a token
                gLog.write("data", "Server issued token to user " + username + ".", "");
                return yourToken;
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
            }
        }

        // if we never returned the actual token, this will return null
        return null;
    }

    //Method to create the keychain table for all blocks from all groups a user can have access too
    private Hashtable createKeyChainTable(String username, UserToken yourToken, String type) {
        int usersStartBlock, usersEndBlock;
        Hashtable<String, ArrayList<String>> keyChainTable = null;
        String requester = yourToken.getSubject();

        //Check if requester exists
        if (my_gs.userList.checkUser(requester)) {
            ArrayList<String> userGroups = my_gs.userList.getUserGroups(requester);
            if (userGroups.size() > 0) {
                keyChainTable = new Hashtable<String, ArrayList<String>>();
				System.out.println("user group's size is: " + userGroups.size());
                for (int i = 0; i < userGroups.size(); i++) {
					System.out.println("loop iter: " + i);
                    // for each of the groups, get its name
                    String groupName = userGroups.get(i);
                    // get its full key chains
					ArrayList<SecretKey> fullKeyList = null;
					if(type.equals("e")) {
						fullKeyList = my_gs.keyList.getGroupsKeyEncChain(groupName);
						System.out.println("fullkeylist size is: " + fullKeyList.size());
					} else {
						fullKeyList = my_gs.keyList.getGroupsKeyIntChain(groupName);
						System.out.println("fullkeylist size is: " + fullKeyList.size());
					}
					
                    // get the users start and end blocks
                    usersStartBlock = my_gs.userList.getUserStartBlock(requester, groupName);
                    if (!(my_gs.userList.isUserCurrentlyActive(requester, groupName))) {
                        usersEndBlock = my_gs.userList.getUserEndBlock(requester, groupName);
                    } else {
                        usersEndBlock = fullKeyList.size() - 1;
                    }

                    // now make the new partialKeyList for what they have access too
                    ArrayList<String> partialKeyList = new ArrayList<String>();
					System.out.println("userEndBlock is: " + usersEndBlock);
					System.out.println("userstartBlock is: " + usersStartBlock);
                    for (int j = 0; j < fullKeyList.size(); j++) {
                        if (j < usersStartBlock || j > usersEndBlock) {
                            partialKeyList.add("-1");
                        } else {
                            String stringifiedKey = new String(Base64.getEncoder().encode(fullKeyList.get(j).getEncoded()));
							System.out.println("stringkey is: " + stringifiedKey);
                            partialKeyList.add(stringifiedKey);
                        }
                    }
                    // add that partial arraylist into the retun hashtable using the group name as the index
                    keyChainTable.put(groupName, partialKeyList);
					System.out.println("put was successful");
                }
                return keyChainTable;
            }
        }
        return null;
    }

    //Method to create a user
    private boolean createUser(String username, UserToken yourToken) {
        String requester = yourToken.getSubject();
        //Check if requester exists
        if (my_gs.userList.checkUser(requester)) {
            //Get the user's groups
            ArrayList<String> temp = my_gs.userList.getUserGroups(requester);
            //requester needs to be an administrator
            if (temp.contains("ADMIN")) {
                //Does user already exist?
                if (!my_gs.userList.checkUser(username)) {
                    my_gs.userList.addUser(username);
                    // log the server created the user
                    gLog.write("data", "User \"" + username + "\" was successfully created by " + requester + ".", "");
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //Method to delete a user
    private boolean deleteUser(String username, UserToken yourToken) {
        String requester = yourToken.getSubject();
        //Does requester exist?
        if (my_gs.userList.checkUser(requester)) {
            ArrayList<String> temp = my_gs.userList.getUserGroups(requester);
            //requester needs to be an administer
            if (temp.contains("ADMIN")) {
                //Does user exist?
                if (my_gs.userList.checkUser(username)) {
                    //User needs deleted from the groups they belong
                    ArrayList<String> deleteFromGroups = new ArrayList<String>();

                    //This will produce a hard copy of the list of groups this user belongs
                    for (int index = 0; index < my_gs.userList.getUserGroups(username).size(); index++) {
                        deleteFromGroups.add(my_gs.userList.getUserGroups(username).get(index));
                    }

                    for (int i = 0; i < deleteFromGroups.size(); i++) {
                        my_gs.groupList.removeUser(deleteFromGroups.get(i), username);
                    }

                    //If groups are owned, they must be deleted
                    ArrayList<String> deleteOwnedGroup = new ArrayList<String>();

                    //Make a hard copy of the user's ownership list
                    for (int index = 0; index < my_gs.userList.getUserOwnership(username).size(); index++) {
                        deleteOwnedGroup.add(my_gs.userList.getUserOwnership(username).get(index));
                    }

                    //Delete owned groups
                    for (int index = 0; index < deleteOwnedGroup.size(); index++) {
                        //Use the delete group method. Token must be created for this action
                        deleteGroup(deleteOwnedGroup.get(index), new Token(my_gs.name, username, deleteOwnedGroup, "-1", "-1", null));
                    }

                    //Delete the user from the user list
                    my_gs.userList.deleteUser(username);
                    gLog.write("data", "User \"" + username + "\" was successfully deleted by " + requester + ".", "");
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //Method to create a group
    private boolean createGroup(String groupToCreate, UserToken yourToken) {
        byte[] newHashedToken = computeTokenHash(yourToken.getIssuer(), yourToken.getSubject(), yourToken.getGroups(), yourToken.getIPAddress(), yourToken.getPortNumber());
        if (verifySignature(newHashedToken, yourToken.getSignedHash())) {
            String requester = yourToken.getSubject();
            //Check if requester exists
            if (my_gs.userList.checkUser(requester)) {
                if (!my_gs.groupList.checkGroup(groupToCreate)) {
                    // no one is in that group, add requester to group and make them owner
                    my_gs.userList.addGroup(requester, groupToCreate);
                    my_gs.userList.addOwnership(requester, groupToCreate);

                    my_gs.groupList.addGroup(groupToCreate);
                    my_gs.groupList.addUser(groupToCreate, requester);
                    my_gs.groupList.addOwnership(groupToCreate, requester);
					
					my_gs.keyList.newGroupKeyChain(groupToCreate);
					my_gs.userList.addUserStartBlock(requester, groupToCreate, 0);
                    // log that the group was created 
                    gLog.write("data", "Group \"" + groupToCreate + "\" was successfully created by " + requester + ".", "");
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //Method to delete a group
    private boolean deleteGroup(String groupToDelete, UserToken yourToken) {
        String requester = yourToken.getSubject();
        //Check if requester exists
        if (my_gs.userList.checkUser(requester)) {
            // check that they own the group or are an admin in order to delete it
            if (my_gs.userList.getUserOwnership(requester).contains(groupToDelete) || my_gs.userList.getUserGroups(requester).contains("ADMIN")) {
                // check if group exists
                if (my_gs.groupList.checkGroup(groupToDelete)) {
                    ArrayList<String> allToRemove = my_gs.groupList.getGroupsUsers(groupToDelete);
                    // make sure the userlist shows that each user in that group is removed
                    for (int i = 0; i < allToRemove.size(); i++) {
                        // remove them from the group, will check if it contains if it doesnt wont affect output, refer to UserList.java
                        my_gs.userList.removeGroup(allToRemove.get(i), groupToDelete);
                    }
                    my_gs.groupList.deleteGroup(groupToDelete);
                    // all users have been removed from the group
                    // log no more users in group! 
                    gLog.write("data", "Group \"" + groupToDelete + "\" was successfully deleted by " + requester + ".", "");
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //Method to list all of the memebers of a group
    private ArrayList<String> listMembersOfGroup(String groupToList, UserToken yourToken) {
        String requester = yourToken.getSubject();
        // wholelist will either  be null or contain list of members
        ArrayList<String> daWholeList = null;
        // if requester doesnt exist, dont show anything
        if (my_gs.userList.checkUser(requester)) {
            // they must either own the group or be an admin to see the members
            if (my_gs.userList.getUserOwnership(requester).contains(groupToList) || my_gs.userList.getUserGroups(requester).contains("ADMIN")) {
                // check if the group exist
                if (my_gs.groupList.checkGroup(groupToList)) {
                    ArrayList<String> daMemberList = new ArrayList<String>();

                    for (int index = 0; index < my_gs.groupList.getGroupsUsers(groupToList).size(); index++) {
                        daMemberList.add(my_gs.groupList.getGroupsUsers(groupToList).get(index));
                    }

                    System.out.println("User " + yourToken.getSubject() + " listed members of Group:" + groupToList + ". Results:");
                    for (int i = 0; i < daMemberList.size(); i++) {
                        System.out.println("User: " + daMemberList.get(i).toString());
                    }

                    if (daMemberList.size() != 0) {
                        // log someone viewed a group's member list
                        gLog.write("data", "User \"" + requester + "\" requested to view group \"" + groupToList + "\"'s member list.", "");
                        return daMemberList;
                    } else {
                        System.out.println("The group is currently empty.");
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean addUserToGroup(String username, String groupAddingTo, UserToken yourToken) {
        String requester = yourToken.getSubject();
        // check that requester is user and the user they want to delete is valid
        if (my_gs.userList.checkUser(requester)) {
            // either admin or owner of group needed to add a user to the group
            if (my_gs.userList.getUserOwnership(requester).contains(groupAddingTo) || my_gs.userList.getUserGroups(requester).contains("ADMIN")) {
                // check if user exist
                if (my_gs.userList.checkUser(username)) {
                    // check if the group exist
                    if (my_gs.groupList.checkGroup(groupAddingTo)) {
                        // check if that user isnt already in the group
                        if (!my_gs.groupList.getGroupsUsers(groupAddingTo).contains(username)) {
                            my_gs.userList.addGroup(username, groupAddingTo);
                            my_gs.groupList.addUser(groupAddingTo, username);
							
							my_gs.keyList.addKeysToGroup(groupAddingTo);
							System.out.println("new key was added!");
                            int currentBlock = my_gs.keyList.getCurBlockNum(groupAddingTo);
							System.out.println("current block num is: " + currentBlock);
                            my_gs.userList.addUserStartBlock(username, groupAddingTo, currentBlock);
							System.out.println("added user start block!");
							
                            // log here 
                            gLog.write("data", "User \"" + username + "\" was successfully added to the group \"" + groupAddingTo + "\" by " + requester + ".", "");
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean deleteUserFromGroup(String username, String groupDeletingFrom, UserToken yourToken) {
        String requester = yourToken.getSubject();
        // check that requester is user and the user they want to delete is valid
        if (my_gs.userList.checkUser(requester)) {
            // either admin or owner of group needed to delete a user from the group
            if (my_gs.userList.getUserOwnership(requester).contains(groupDeletingFrom) || my_gs.userList.getUserGroups(requester).contains("ADMIN")) {
                // check if the user exist
                if (my_gs.userList.checkUser(username)) {
                    if (my_gs.groupList.checkGroup(groupDeletingFrom)) {
                        // check if that user is in the group they are being deleted from
                        if (my_gs.groupList.getGroupsUsers(groupDeletingFrom).contains(username)) {
                            my_gs.userList.removeGroup(username, groupDeletingFrom);
                            my_gs.groupList.removeUser(groupDeletingFrom, username);

                            // check if user was owner of group, if so remove ownership, make admin the owner
                            if (my_gs.groupList.getGroupOwnership(groupDeletingFrom).equals(username)) {
                                my_gs.groupList.removeOwnership(username, groupDeletingFrom);
                                my_gs.groupList.addOwnership(yourToken.getSubject(), groupDeletingFrom);
                                my_gs.groupList.removeOwnership(groupDeletingFrom, username);
                                my_gs.groupList.addOwnership(groupDeletingFrom, yourToken.getSubject());
                            }

                            // log that the server was successfully ran
                            gLog.write("data", "User \"" + username + "\" was successfully deleted from the group \"" + groupDeletingFrom + "\" by " + requester + ".", "");
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
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

    private boolean verifySignature(byte[] computedHash, byte[] signedHash) {
        byte[] returnHash = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            PemReader daPubPemReader = new PemReader(new InputStreamReader(new FileInputStream("SystemKeys/gs-pubK.pem")));
            PemObject daPubPemObj = daPubPemReader.readPemObject();
            byte[] publicBytes = daPubPemObj.getContent();
            X509EncodedKeySpec pubEncoded = new X509EncodedKeySpec(publicBytes);
            KeyFactory daFactory = KeyFactory.getInstance("RSA");
            PublicKey pubK = daFactory.generatePublic(pubEncoded);

            Signature signature = Signature.getInstance("SHA256withRSA", "BC");
            signature.initVerify(pubK);
            signature.update(computedHash);
            if (signature.verify(signedHash)) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
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
