
import java.util.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

// import the bouncy castle provider
import org.bouncycastle.jce.provider.BouncyCastleProvider;
// needed to encode the keys and write them as PEM files
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class KeyGenerator {

    public static void main(String[] args) {
        // check if MyKeys folder exist, where the keys files will be saved
        File directory = new File("MyKeys");
        if (!directory.exists()) {
            System.out.println("\"MyKeys\" directory was not found. Creating now...");
            directory.mkdir();
            System.out.println("Directory was successfully created!");
        }

        System.out.println("Welcome to the Key Generator!");

        Scanner input = new Scanner(System.in);
        int inputChoice = -1;
        while (true) {
            System.out.println();
            System.out.println("Enter 1 to generate user keys");
            System.out.println("Enter 2 to generate server keys");
            System.out.println("Enter 3 to exit the KeyGenerator");
            System.out.print("Enter choice: ");
            try {
                inputChoice = Integer.parseInt(input.nextLine());
            } catch (Exception e) {
            }

            // they want to create a user key pair
            if (inputChoice == 1) {
                String inputName = "", fName = "";
                // prompt for the username of the user who needs keys
                System.out.print("Enter user's username: ");
                inputName = input.nextLine();
                fName = "MyKeys/user-" + inputName + "-";

                // attempt to generate their keys
                if (generateKeyPair(fName)) {
                    System.out.println("Key files successfully created in \"MyKeys\" directory!");
                } else {
                    System.out.println("Key files were NOT successfully created for the user!");
                }
            } else if (inputChoice == 2) {
                String fName = "";
                int serverChoice = -1;
                while (true) {
                    System.out.println();
                    System.out.println("Enter 1 to generate group server keys");
                    System.out.println("Enter 2 to generate file server keys");
                    System.out.println("Enter 3 to go back to the main menu");
                    System.out.print("Enter choice : ");
                    try {
                        serverChoice = Integer.parseInt(input.nextLine());
                    } catch (Exception e) {
                    }

                    boolean success = false;
                    if (serverChoice == 1) {
                        success = generateKeyPair("MyKeys/gs-");
                    } else if (serverChoice == 2) {
                        success = generateKeyPair("MyKeys/fs-");
                    } else if (serverChoice == 3) {
                        break;
                    } else {
                        System.out.println("Invalid choice. Try again.");
                    }

                    if (success == true) {
                        System.out.println("Key files successfully created in \"MyKeys\" directory!");
                    } else {
                        System.out.println("Key files were NOT successfully created for the server!");
                    }
                }
            } else if (inputChoice == 3) {
                break;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
        System.out.println("Now exiting Key Generator. Goodbye!");
    }

    // will take a mode (user or servers) 
    private static boolean generateKeyPair(String fileName) {
        try {
            System.out.println("Generating 4096-bit keys...");
            // use Bouncy Castle as the security provider
            Security.addProvider(new BouncyCastleProvider());

            // create the two 4096 bit keys
            KeyPairGenerator theGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            theGenerator.initialize(4096, new SecureRandom());
            KeyPair daKeys = theGenerator.generateKeyPair();
            Key pubK = daKeys.getPublic();
            Key privK = daKeys.getPrivate();

            // setup the PEM file writers and encode the keys to be written to the files
            String publicKeyFileName = fileName + "pubK.pem";
            String privateKeyFileName = fileName + "privK.pem";
            PemObject pubPem = new PemObject("4096-bit Public Key", pubK.getEncoded());
            PemObject privPem = new PemObject("4096-bit Private Key", privK.getEncoded());
            PemWriter daPubPemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(publicKeyFileName)));
            PemWriter daPrivPemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(privateKeyFileName)));

            // write the encoded keys and close the writers
            daPubPemWriter.writeObject(pubPem);
            daPrivPemWriter.writeObject(privPem);
            daPubPemWriter.close();
            daPrivPemWriter.close();

            // made it here so everything went fine
            System.out.println("Created PEM file: " + publicKeyFileName.substring(publicKeyFileName.indexOf("/") + 1, publicKeyFileName.length()));
            System.out.println("Created PEM file: " + privateKeyFileName.substring(privateKeyFileName.indexOf("/") + 1, privateKeyFileName.length()));
            return true;
        } catch (Exception e) {
            // something went wrong, show them the error and return
            System.out.println(e.getMessage());
            System.out.println("ERROR: Key files could not be created.");
            return false;
        }
    }
}
