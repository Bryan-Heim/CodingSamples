
import java.util.*;
import java.io.*;

public class GroupClientApplication {

    // to log group related info and errors

    DataLogger gLog = new DataLogger("Group");
    StringWriter stackTraceString = new StringWriter();
    PrintWriter stackTrace = new PrintWriter(stackTraceString);

    // create client object and network data vars and input reader
    GroupClient groupClient;
    String serverName = "";
    int portNumber = -1;
    UserToken daToken = null;
    // to get user input
    Scanner input = new Scanner(System.in);

    public GroupClientApplication(String serverNameIn, int portNumberIn) {
        serverName = serverNameIn;
        portNumber = portNumberIn;
        daToken = null;
        groupClient = new GroupClient();
    }

	// load in the required info needed to connect to the server and establish identity
    // these will be initialized when ClientApplication (main driver for program) creates this class
    public GroupClientApplication(String serverNameIn, int portNumberIn, UserToken daTokenIn) {
        serverName = serverNameIn;
        portNumber = portNumberIn;
        daToken = daTokenIn;
        groupClient = new GroupClient();
    }

    // helper functions unrelated to the actual work, includes prints
    public void displayPrompt() {
        // to validate the input
        boolean validInput = false;
        // for which command they want to run
        String choice = "";
        String daUsername = daToken.getSubject();
		//if(groupClient.connect(serverName,portNumber))
        //{
        System.out.println();
        System.out.println("Welcome to the Bryan-Richard Group Server!");
        do {
            // try to get a new token in-case group related information changed
            System.out.println();
            System.out.println("You can run the following commands:");
            System.out.println("  CU - Create User");
            System.out.println("  DU - Delete User");
            System.out.println("  CG - Create Group");
            System.out.println("  DG - Delete Group");
            System.out.println("  LM - List Group Members");
            System.out.println("  UA - Add User to Group");
            System.out.println("  UD - Delete User from Group");
            System.out.println("  EX - Terminate the current session.");
            System.out.println("(To enter command, use 2 leading chars)");
            System.out.print("Enter command: ");

            // check if command was valid, keep looping until valid
            try {
                do {
                    choice = input.nextLine();
                    choice = choice.substring(0, 2).toUpperCase();
                    if (choice.equals("CU") || choice.equals("DU") || choice.equals("CG") || choice.equals("DG") || choice.equals("UA") || choice.equals("UD") || choice.equals("LM") || choice.equals("EX")) {
                        validInput = true;
                    } else {
                        System.out.println("Invalid command. Enter command: ");
                    }
                } while (validInput != true);
            } catch (Exception e) {
                System.out.println("Error: unable to accept input");
                e.printStackTrace(new PrintWriter(stackTrace));
                gLog.write("err", e.getMessage(), stackTraceString.toString());
                return;
            }

            // now we have a valid input, call proper method to handle
            if (choice.equals("CU")) {
                createUser();
            } else if (choice.equals("DU")) {
                deleteUser();
            } else if (choice.equals("CG")) {
                createGroup();
            } else if (choice.equals("DG")) {
                deleteGroup();
            } else if (choice.equals("LM")) {
                listMembers();
            } else if (choice.equals("UA")) {
                addUserToGroup();
            } else if (choice.equals("UD")) {
                deleteUserFromGroup();
            }

        } while (!(choice.equals("EX")));

        // the have chose to exit, show message and disconnect.
        System.out.println("Terminating Session...\nExiting the Group Server. Goodbye!");
		//}
        //else
        //	System.out.println("Connection was not established. Now terminating session.");
        //return;
    }

    // run the actual commands from the GroupClient class
    private void createUser() {
        // we must check that they are an admin to create and delete users
        String userNameAttempt = "";
        try {
            System.out.print("Enter the username to create: ");
            userNameAttempt = input.nextLine();
            // have the GroupClient send the command to the server, check if server sends success or not
            if (groupClient.createUser(userNameAttempt, daToken)) {
                System.out.println("The user \"" + userNameAttempt + "\" was successfully created!");
            } else {
                System.out.println("Error: User \"" + userNameAttempt + "\" was not created.\nIt either already exist or you lack the rights to create users.");
            }
        } catch (Exception e) {
            System.out.println("Error: Unable to accept input");
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
        }
        return;
    }

    private void deleteUser() {
        String userNameAttempt = "";
        try {
            System.out.print("Enter the username to delete: ");
            userNameAttempt = input.nextLine();
            // same as createUser but now for deleting
            if (groupClient.deleteUser(userNameAttempt, daToken)) {
                System.out.println("The user \"" + userNameAttempt + "\" was successfully deleted!");
            } else {
                System.out.println("Error: User \"" + userNameAttempt + "\" was not deleted.\nIt either does not exist or you lack the rights to delete users.");
            }
        } catch (Exception e) {
            System.out.println("Error: Unable to accept input");
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
        }
        return;
    }

    private void createGroup() {
        String groupToCreate = "";
        try {
            System.out.print("Enter the group to create: ");
            groupToCreate = input.nextLine();

            if (groupClient.createGroup(groupToCreate, daToken)) {
                System.out.println("The group " + groupToCreate + " was successfully created!");
            } else {
                System.out.println("Error: Group creation failed. The group already exist.");
            }
        } catch (Exception e) {
            System.out.println("Error: Unable to accept input");
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
        }
        return;
    }

    private void deleteGroup() {
        String groupToDel = "";
        try {
            System.out.print("Enter the group to delete: ");
            groupToDel = input.nextLine();

            if (groupClient.deleteGroup(groupToDel, daToken)) {
                System.out.println("The group " + groupToDel + " was successfully deleted!");
            } else {
                System.out.println("Error: Group \"" + groupToDel + "\" was not deleted.\nIt either does not exist or you lack the rights to delete this group.");
            }
        } catch (Exception e) {
            System.out.println("Error: Unable to accept input");
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
        }
        return;
    }

    private void listMembers() {
        String groupToList = "";
        try {
            System.out.print("Enter the group name to list members: ");
            groupToList = input.nextLine();

            ArrayList<String> memList = new ArrayList<String>(groupClient.listMembers(groupToList, daToken));
            if (memList != null) {
                System.out.println("The group " + groupToList + " has the following members:");
                for (int i = 0; i < memList.size(); i++) {
                    System.out.println("User: " + memList.get(i));
                }
            } else {
                System.out.println("Error: Unable to list members from the group \"" + groupToList + "\".\nEither group does not exist/is empty or\nYou lack the rights to list the members from that group.");
            }
        } catch (Exception e) {
            System.out.println("Error: Unable to list members from the group \"" + groupToList + "\".\nEither group does not exist/is empty or\nYou lack the rights to list the members from that group.");
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
        }
        return;
    }

    private void addUserToGroup() {
        String userToAdd = "", groupAddingTo = "";
        try {
            System.out.print("Enter the user to add: ");
            userToAdd = input.nextLine();
            System.out.print("Enter the grouping adding to: ");
            groupAddingTo = input.nextLine();

            if (groupClient.addUserToGroup(userToAdd, groupAddingTo, daToken)) {
                System.out.println("The user \"" + userToAdd + "\" was successfully added to the group \"" + groupAddingTo + "\"!");
            } else {
                System.out.println("Error: User \"" + userToAdd + "\" was not added to group \"" + groupAddingTo + "\".\nEither the user or group do not exist or\nThe user is not a member of that group or\nYou lack the rights to add users to that group.");
            }
        } catch (Exception e) {
            System.out.println("Error: Unable to accept input");
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
        }
        return;
    }

    private void deleteUserFromGroup() {
        String userToDelete = "", groupDeletingFrom = "";
        try {
            System.out.print("Enter the user to delete: ");
            userToDelete = input.nextLine();
            System.out.print("Enter the grouping deleting from: ");
            groupDeletingFrom = input.nextLine();

            if (groupClient.deleteUserFromGroup(userToDelete, groupDeletingFrom, daToken)) {
                System.out.println("The user \"" + userToDelete + "\" was successfully deleted from group \"" + groupDeletingFrom + "\"!");
            } else {
                System.out.println("Error: User \"" + userToDelete + "\" was not deleted from the group \"" + groupDeletingFrom + "\".\nEither the user or group do not exist or\nThe user is not a member of that group or\nYou lack the rights to delete users from that group.");
            }
        } catch (Exception e) {
            System.out.println("Error: Unable to accept input");
            e.printStackTrace(new PrintWriter(stackTrace));
            gLog.write("err", e.getMessage(), stackTraceString.toString());
        }
        return;
    }
}
