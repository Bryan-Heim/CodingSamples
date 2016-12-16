
import java.util.*;
import javax.crypto.*;

public class ClientApplication {

    public static void main(String[] args) {

		// check if they have MyKeys directory for all their keys
        // must load gs-pubK.pem and any fs-pubK.pem to access either
        Hashtable<String, ArrayList<String>> encKeyChainTable = null;
		Hashtable<String, ArrayList<String>> intKeyChainTable = null;
        FileClientApplication fca;
        GroupClientApplication gca = null;
        UserToken token = null;
        Scanner sc = new Scanner(System.in);
        boolean tokenIssued = false;
        String server = null;
        int port = -1;

        String username = null, s = null;
        do {
            try {
                System.out.print("Enter the group server name: ");
                server = sc.nextLine();
                System.out.print("Enter the port number to use: ");
                String temp1 = sc.nextLine();
                port = Integer.parseInt(temp1);
            } catch (Exception e) {
            }

            gca = new GroupClientApplication(server, port);
            if (gca.groupClient.connect(server, port)) {

                System.out.print("Enter username: ");
                username = sc.nextLine();

                if (gca.groupClient.authenticateUser(username)) {
                    // we have the username, before we do anything we are going to authenticate
                    token = gca.groupClient.getToken(username, "-1", "-1");
					// disconnect this session because key is null and only used to get token
                    //gca.groupClient.disconnect();

                    if (token == null) {
                        System.out.println("Error: Username is not valid. Token was NOT issued.");
                    } else {
                        //if we hit this point, we have verified the login and have obtained a token from the group server.
                        tokenIssued = true;
                        gca.daToken = token;
                        break;
                    }
                } else {
                    System.out.println("Authentication FAILED, NO tokens will be issued!");
                    System.out.println("Ensure the username is correct and registered");
                    System.out.println("Call to ensure the server's public key is correct");
                    System.out.println("Retry once all the above information have been verified");
                }
            } else {
                System.out.println("Error: Unable to connect using server name and port given.");
                System.out.print("Enter \"exit\" to stop attempting to connect. \nEnter anything else to try again:");
                s = sc.nextLine();
            }

        } while ((s != null) && !(s.toLowerCase().equals("exit")));

        if (token != null) {
            System.out.println();
            System.out.println("Welcome " + username + " to the FANTASTICER FileSystem");

            int decision = -1;
            String temp0 = "", answer = "", wantedIP = "-1", wantedPort = "-1";
            while (decision != 3) {
                System.out.print("Accessing a new file server? (y/n): ");
                answer = sc.nextLine();
                if (answer.equals("Y") || answer.equals("y")) {
                    System.out.print("Enter the file server's IP address: ");
                    wantedIP = sc.nextLine();
                    System.out.print("Enter the file server's port number: ");
                    wantedPort = sc.nextLine();
                } else if (answer.equals("N") || answer.equals("n")) {
                    System.out.println("Note! You will need a new token to access a file server!");
                } else {
                    System.out.println("Invalid response. To access a file server obtain a new token.");
                }
                // get a correct token
                token = gca.groupClient.getToken(username, wantedIP, wantedPort);
                // get a correct keyChainTable 
				gca.groupClient.setKeyChainTables(username, token);
                encKeyChainTable = gca.groupClient.encKeyChainTable;
				intKeyChainTable = gca.groupClient.intKeyChainTable;

                fca = new FileClientApplication();
                decision = -1;
                try {
                    while (decision != 1 && decision != 2 && decision != 3) {
                        System.out.println();
						System.out.println(username + "'s System Menu");
                        System.out.println("------------------------------------------------");
                        System.out.println("Press 1 to use the group server.");
                        System.out.println("Press 2 to use a file server.");
                        System.out.println("Press 3 to exit this client.");
                        System.out.println("------------------------------------------------");
                        System.out.print("Enter command: ");
                        temp0 = sc.nextLine();
                        decision = Integer.parseInt(temp0);
                    }
                } catch (Exception e) {
                }

                if (decision == 1) {
                    gca.displayPrompt(); // assuming the group server is on same port and with same name
                } else if (decision == 2) {
                    port = -1;
                    server = null;
                    boolean connected = false;
                    String temp = "";

                    do {
                        try {
                            System.out.print("Enter the name of the File Server: ");
                            server = sc.nextLine(); //sc.next();
                            System.out.print("Enter the port number to connect to: ");
                            String temp2 = sc.nextLine();
                            port = Integer.parseInt(temp2);

                            if (fca.connect(server, port)) {
                                connected = true;
                                break;
                            }
                        } catch (Exception e) {
                        }
                        System.out.print("Error: Could not connect to file server.\nEnter \"exit\" to quit, anything else to try again: ");
                        temp = sc.nextLine();
                    } while (!(temp.toLowerCase().equals("exit")));

                    // if they connected to the port, 
                    if (connected == true) {
                        fca.start(username, token, encKeyChainTable, intKeyChainTable);
                    }
                }
            }
        } else {
            System.out.println("Unable to verify user token. You need a valid token to interact with the system.");
        }
        gca.groupClient.disconnect();
        System.out.println("Now terminating the current client. Goodbye!");
    }
}
