
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

/**
 *
 * @author Richard Dillon
 */
public class FileClientApplication {

    private FileClient fc = new FileClient();

    public boolean connect(final String server, final int port) {

        if (fc.connect(server, port)) {
            //now we have to authenticate the file server.
            File syskeys = new File("MyKeys");
            if (!syskeys.exists()) {
                System.out.println("Error: MyKeys folder not found.");
                System.out.println("Making folder MyKeys...");
                syskeys.mkdir();
                System.out.println("MyKeys folder has been made.");
                System.out.println("Store the fs-pubK.pem file, of the server you are trying to connect to, in the MyKeys folder.");
                return false;
            } else {
                try {
                    PemReader daPubPemReader = new PemReader(new InputStreamReader(new FileInputStream("MyKeys/fs-pubK.pem")));
                    PemObject daPubPemObj = daPubPemReader.readPemObject();
                    byte[] publicBytes = daPubPemObj.getContent();
                    X509EncodedKeySpec pubEncoded = new X509EncodedKeySpec(publicBytes);
                    KeyFactory daFactory = KeyFactory.getInstance("RSA");
                    PublicKey serverPubK = daFactory.generatePublic(pubEncoded);
                    //now we have recovered the servers public key.

                    daPubPemReader.close();

                    //Generate a 256-bit AES key, and 160 bit random challenge
                    Provider BC = new BouncyCastleProvider();
                    javax.crypto.KeyGenerator kg = javax.crypto.KeyGenerator.getInstance("AES", BC);
                    kg.init(256, new SecureRandom());
                    SecretKey confidentialityAES = kg.generateKey();
                    kg.init(256, new SecureRandom());
                    SecretKey integrityAES = kg.generateKey();
                    fc.sessionConfidentialityAESKey = confidentialityAES;
                    fc.sessionIntegrityAESKey = integrityAES;
                    byte[] challenge = new byte[20];//160 bit random challenge.
                    fc.sr.nextBytes(challenge);
                    //now we have the AES key, the challenge, and the server's public key.

                    //send challenge and aes key, encrypted with the server's public key, to the server.
                    Cipher cipher = Cipher.getInstance("RSA", BC);
                    cipher.init(Cipher.ENCRYPT_MODE, serverPubK);
                    Envelope env = new Envelope("AESAndChallenge");
                    byte[] concat = new byte[84];
                    byte[] encoded = confidentialityAES.getEncoded();
                    byte[] encoded1 = integrityAES.getEncoded();
                    System.arraycopy(encoded, 0, concat, 0, encoded.length);//copying the encoded key, and the challenge into a concat byte array
                    System.arraycopy(encoded1, 0, concat, encoded.length, encoded1.length);
                    System.arraycopy(challenge, 0, concat, encoded.length + encoded1.length, challenge.length);

                    byte[] encrypted = cipher.doFinal(concat);
                    encrypted = Base64.getEncoder().encode(encrypted);//Base64 encoding the encrypted message.
                    env.addObject(new String(encrypted));
                    fc.output.writeObject(env);
                    env = (Envelope) fc.input.readObject();
                    if (env.getMessage().equals("ChallengeAndEncryptedChallenge")) {
                        String tmp = (String) env.getObjContents().get(0);
                        byte[] challenge1 = Base64.getDecoder().decode(tmp.getBytes());//recovering the hash from the server.
                        if (Arrays.equals(challenge, challenge1)) {
                            byte[] IV = Base64.getDecoder().decode(env.getBytes2());
                            Cipher aes = Cipher.getInstance("AES/CBC/PKCS7PADDING", BC);
                            IvParameterSpec spec = new IvParameterSpec(IV);
                            aes.init(Cipher.DECRYPT_MODE, confidentialityAES, spec);
                            byte[] random1encrypted = Base64.getDecoder().decode(env.getBytes1());
                            byte[] random1 = aes.doFinal(random1encrypted);
                            env = new Envelope("RandomChallenge");
                            env.addBytes1(Base64.getEncoder().encode(random1));
                            fc.output.writeObject(env);
                            return true;
                        } else {
                            return false;
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("FileClientApplication connect, no fs-pubK.pem file found.");
                    return false;
                } catch (IOException ex) {
                    System.out.println("FileClientApplication connect, cannot read pem object from fs-pubK.pem file.");
                    return false;
                } catch (NoSuchAlgorithmException ex) {
                    System.out.println("FileClientApplication connect, no such algorithm.");
                    return false;
                } catch (InvalidKeySpecException ex) {
                    System.out.println("FileClientApplication connect, invalid key spec given.");
                    return false;
                } catch (NoSuchPaddingException ex) {
                    System.out.println("FileClientApplication connect, no such padding.");
                    return false;
                } catch (InvalidKeyException ex) {
                    System.out.println("FileClientApplication connect, invalid server public key given.");
                    return false;
                } catch (IllegalBlockSizeException ex) {
                    System.out.println("FileClientApplication connect, illegal block size.");
                    return false;
                } catch (BadPaddingException ex) {
                    System.out.println("FileClientApplication connect, bad padding.");
                    return false;
                } catch (ClassNotFoundException ex) {
                    System.out.println("FileClientApplication connect, class not found.");
                    return false;
                } catch (InvalidAlgorithmParameterException ex) {
                    Logger.getLogger(FileClientApplication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }

    public void start(String username, UserToken token, Hashtable encKeyChainTable, Hashtable intKeyChainTable) {
        // hashtable is in the form <String, ArrayList<SecretKey>>
        Scanner sc = new Scanner(System.in);
        int input = 0;
        while (true) {
            input = 0;
            System.out.println();
            System.out.println("----------------------------------------------------");
            System.out.println("Enter 1 to upload a file.");
            System.out.println("Enter 2 to delete a file.");
            System.out.println("Enter 3 to list all files that you have access to.");
            System.out.println("Enter 4 to download a file.");
            System.out.println("Enter 5 to exit this client.");
            System.out.println("----------------------------------------------------");
            System.out.print("Enter Command: ");
            try {
                input = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid Command. Please try again.");
            }

            if (input == 1) {
                System.out.print("Enter the path to the file that you wish to upload: ");
                String path = sc.nextLine();
                System.out.print("What would you like to name this file on the server?: ");
                String name = sc.nextLine();
                System.out.print("Which group should be allowed access to this file?: ");
                String group = sc.nextLine();
                if (fc.upload(path, name, group, token, encKeyChainTable, intKeyChainTable) == false) {
                    System.out.println("File was unable to be uploaded.");
                } else {
                    System.out.println("File was uploaded successfully");
                }
            } else if (input == 2) {
                System.out.print("Please enter the name of the file that you would like to delete: ");
                String name = sc.nextLine();
                if (fc.delete(name, token) == false) {
                    System.out.println("File was unable to be deleted");
                } else {
                    System.out.println("File was successfully deleted.");
                }
            } else if (input == 3) {
                List<String> l = fc.listFiles(token);
                if (l != null) {
                    for (int i = 0; i < l.size(); i++) {
                        String x = l.get(i);
                        System.out.println(x);
                    }
                } else {
                    System.out.println("Unable to find any files that are accessible by you.");
                }
            } else if (input == 4) {
                System.out.print("Please enter the name of the file that you would like to download: ");
                String name = sc.nextLine();
                System.out.print("What would you like to name this file on your PC? ");
                String name1 = sc.nextLine();
                name1 = name1.replace("\"", "");
                if (fc.download(name, name1, token, encKeyChainTable, intKeyChainTable) == false) {
                    System.out.println("Unable to download file.");
                } else {
                    System.out.println("Successfully downloaded file.");
                }
            } else if (input == 5) {
                fc.disconnect();
                System.out.println("Exiting client application.");
                break;
            }
        }
    }
}
