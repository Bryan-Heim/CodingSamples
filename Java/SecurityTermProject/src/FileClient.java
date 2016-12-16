/* FileClient provides all the client functionality regarding the file server */

import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class FileClient extends Client implements FileClientInterface {

    protected SecretKey sessionConfidentialityAESKey = null;
    protected SecretKey sessionIntegrityAESKey = null;
    protected SecureRandom sr = new SecureRandom();
    protected BouncyCastleProvider BC = new BouncyCastleProvider();
    protected int counter = 0;
    protected Hashtable<String, ArrayList<String>> keyChainTable = null;
    protected Hashtable<String, ArrayList<String>> intChainTable = null;

    public boolean delete(String filename, UserToken token) {
        String remotePath;
        if (filename.charAt(0) == '/') {
            remotePath = filename.substring(1);
        } else {
            remotePath = filename;
        }
        Envelope env = new Envelope("DELETEF"); //Success

        env.addObject(remotePath);
        env.addObject(token);

        encrypt(env, output);
        env = (Envelope) decrypt(input);

        if (env.getMessage().compareTo("OK") == 0) {
            System.out.printf("File %s deleted successfully\n", filename);
        } else {
            System.out.printf("Error deleting file %s (%s)\n", filename, env.getMessage());
            return false;
        }

        return true;
    }

    public boolean download(String sourceFile, String destFile, UserToken token, Hashtable kcTable, Hashtable intTable) {
        keyChainTable = kcTable;
        intChainTable = intTable;
        if (sourceFile.charAt(0) == '/') {
            sourceFile = sourceFile.substring(1);
        }

        File file = new File(destFile);
        try {

            if (!file.exists()) {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);

                Envelope env = new Envelope("DOWNLOADF"); //Success
                env.addObject(sourceFile);
                env.addObject(token);
                encrypt(env, output);

                env = (Envelope) decrypt(input);

                ArrayList<String> keys = keyChainTable.get((String) env.getObjContents().get(2));
                ArrayList<String> intKeys = intChainTable.get((String) env.getObjContents().get(2));
                byte[] IV;
                int keyNumber = (Integer) env.getObjContents().get(3);
                IvParameterSpec spec;
                if (keys.get(keyNumber).equals("-1")) {//this will only work if the key is a String as follows: "-1"
                    //this user does not have access to this key, therefore it is not in the keychain.
                    System.out.println("The key needed to decrypt this file is not in your keychain.");
                    sock.close();
                    System.exit(0);
                }
                byte[] encodedKey = Base64.getDecoder().decode(keys.get(keyNumber).getBytes());
                byte[] encodedKey1 = Base64.getDecoder().decode(intKeys.get(keyNumber).getBytes());
                SecretKey recoveredKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
                SecretKey intKey = new SecretKeySpec(encodedKey1, 0, encodedKey1.length, "AES");
                int k = 0;
                while (env.getMessage().compareTo("CHUNK") == 0) {
                    k += 1;
                    spec = new IvParameterSpec(((String) env.getObjContents().get(4)).getBytes());
                    System.out.println(((String) env.getObjContents().get(4)).getBytes());
                    byte[] encChunk = (byte[]) env.getObjContents().get(0);
                    Cipher aes = Cipher.getInstance("AES/CBC/PKCS7PADDING", BC);
                    System.out.println(recoveredKey);
                    System.out.println(spec);
                    byte[] decryptedInput = null;
                    try {
                        aes.init(Cipher.DECRYPT_MODE, recoveredKey, spec);
                        decryptedInput = new byte[aes.getOutputSize(encChunk.length)];
                        // now decrypt
                        int decryptedLength = aes.update(encChunk, 0, encChunk.length, decryptedInput, 0);
                        decryptedLength += aes.doFinal(decryptedInput, decryptedLength);
                    } catch (Exception e) {
                    }

                    fos.write(decryptedInput, 0, (Integer) env.getObjContents().get(1));
                    System.out.printf(".");
                    env = new Envelope("DOWNLOADF"); //Success
                    encrypt(env, output);
                    env = (Envelope) decrypt(input);
                }
                fos.close();

                if (env.getMessage().compareTo("EOF") == 0) {
                    fos.close();
                    System.out.printf("\nTransfer successful file %s\n", sourceFile);

                    byte[] temp = new byte[k * 4096];
                    FileInputStream fis = new FileInputStream(file);
                    fis.read(temp);

                    byte[] serverHMAC = Base64.getDecoder().decode((String) env.getObjContents().get(0));
                    Mac mac = Mac.getInstance("HmacSHA256", new BouncyCastleProvider());
                    mac.init(intKey);
                    mac.update(temp);
                    byte[] calculatedHMAC = mac.doFinal();
                    if (!Arrays.equals(serverHMAC, calculatedHMAC)) {
                        System.out.println("ERROR: The file you downloaded has been tampered with.  If this continues to happen, consider using another file server.");
                    }

                    env = new Envelope("OK"); //Success
                    encrypt(env, output);
                } else {
                    System.out.printf("Error reading file %s (%s)\n", sourceFile, env.getMessage());
                    file.delete();
                    return false;
                }
            } else {
                System.out.printf("Error couldn't create file %s\n", destFile);
                return false;
            }

        } catch (IOException e1) {

            System.out.printf("Error couldn't create file %s\n", destFile);
            return false;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(FileClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public List<String> listFiles(UserToken token) {
        try {
            Envelope message = null, e = null;
            //Tell the server to return the member list
            message = new Envelope("LFILES");
            message.addObject(token); //Add requester's token
            encrypt(message, output);

            e = (Envelope) decrypt(input);

            //If server indicates success, return the member list
            if (e.getMessage().equals("OK")) {
                return (List<String>) e.getObjContents().get(0); //This cast creates compiler warnings. Sorry.
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
            return null;
        }
    }

    public boolean upload(String sourceFile, String destFile, String group,
            UserToken token, Hashtable kcTable, Hashtable intTable) {
        keyChainTable = kcTable;
        intChainTable = intTable;
        sourceFile = sourceFile.replace("\"", "");
        if (destFile.charAt(0) != '/') {
            destFile = "/" + destFile;
        }

        try {

            Envelope message = null, env = null;
            //Tell the server to return the member list
            message = new Envelope("UPLOADF");
            message.addObject(destFile);
            message.addObject(group);
            message.addObject(token); //Add requester's token
            encrypt(message, output);

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(sourceFile);
            } catch (Exception e) {
                System.out.println("File does not exist at given path");
            }

            env = (Envelope) decrypt(input);

            //If server indicates success, return the member list
            if (env.getMessage().equals("READY")) {
                System.out.printf("Meta data uploaded successfully\n");

            } else {

                System.out.printf("Upload failed: %s\n", env.getMessage());
                return false;
            }
            byte[] IV;
            Cipher aes = Cipher.getInstance("AES/CBC/PKCS7PADDING", BC);
            IvParameterSpec spec;
            ArrayList<String> keys = keyChainTable.get(group);// I am assuming that kcTable is <string, arraylist<string>>
            ArrayList<String> intKeys = (ArrayList<String>) intTable.get(group);
            int keyNumber = keys.size() - 1;
            byte[] encodedKey = Base64.getDecoder().decode(keys.get(keyNumber).getBytes());
            byte[] encodedKey1 = Base64.getDecoder().decode(intKeys.get(keyNumber).getBytes());
            SecretKey originalKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
            SecretKey integKey = new SecretKeySpec(encodedKey1, 0, encodedKey1.length, "AES");

            int k = 0;
            do {
                k += 1;
                IV = new byte[16];
                sr.nextBytes(IV);
                System.out.println(new String(IV));
                spec = new IvParameterSpec(IV);
                byte[] buf = new byte[4096];

                if (env.getMessage().compareTo("READY") != 0) {
                    System.out.printf("Error: %s\n", env.getMessage());
                    return false;
                }
                message = new Envelope("CHUNK");
                int n = -1;
                try {
                    n = fis.read(buf); //can throw an IOException
                    if (n <= 0) {
                        System.out.println("Read error");
                        return false;
                    }
                } catch (Exception e) {
                    System.out.println("Read Error");
                    return false;
                }

                aes.init(Cipher.ENCRYPT_MODE, originalKey, spec);
                byte[] encryptedInput = new byte[aes.getOutputSize(buf.length)];
                // now encrypt
                int encryptedLength = aes.update(buf, 0, buf.length, encryptedInput, 0);
                encryptedLength += aes.doFinal(encryptedInput, encryptedLength);

                message.addObject(encryptedInput);
                message.addObject(new Integer(n));
                message.addObject(new String(IV));

                encrypt(message, output);

                env = (Envelope) decrypt(input);

            } while (fis.available() > 0);

            //If server indicates success, return the member list
            if (env.getMessage().compareTo("READY") == 0) {
                byte[] temp = new byte[k * 4096];
                fis = new FileInputStream(sourceFile);
                fis.read(temp);
                Mac mac = Mac.getInstance("HmacSHA256", new BouncyCastleProvider());
                mac.init(integKey);
                mac.update(temp);
                byte[] calculatedHMAC = mac.doFinal();
                message = new Envelope("EOF");
                message.addObject(new Integer(keyNumber));
                message.addObject(Base64.getEncoder().encodeToString(calculatedHMAC));
                encrypt(message, output);

                env = (Envelope) decrypt(input);
                if (env.getMessage().compareTo("OK") == 0) {
                    System.out.printf("\nFile's contents uploaded successfully\n");
                } else {

                    System.out.printf("\nUpload failed: %s\n", env.getMessage());
                    return false;
                }

            } else {

                System.out.printf("Upload failed: %s\n", env.getMessage());
                return false;
            }

        } catch (Exception e1) {
            System.err.println("Error: " + e1.getMessage());
            e1.printStackTrace(System.err);
            return false;
        }
        return true;
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
