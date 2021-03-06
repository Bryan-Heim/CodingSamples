/* Group server. Server loads the users from UserList.bin.
 * If user list does not exists, it creates a new list and makes the user the server administrator.
 * On exit, the server saves the user list to file. 
 */

/*
 * TODO: This file will need to be modified to save state related to
 *       groups that are created in the system
 *
 */
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.*;

public class GroupServer extends Server {

    public static final int SERVER_PORT = 8765;
    public UserList userList;
    public GroupList groupList;
    public GroupKeyList keyList;

    public GroupServer() {
        super(SERVER_PORT, "ALPHA");
    }

    public GroupServer(int _port) {
        super(_port, "ALPHA");
    }

    public void start() {
        // Overwrote server.start() because if no user file exists, initial admin account needs to be created
        System.out.println("GroupServer up and running");

        String userFile = "UserList.bin";
        String groupFile = "GroupList.bin";
        String keysFile = "GroupKeyList.bin";
        ObjectInputStream userStream;
        ObjectInputStream groupStream;
        ObjectInputStream keyStream;

        // check if the SystemKey folder are set up and has both priv and pub keys
        File sysDir = new File("SystemKeys");
        if (sysDir.exists()) {
            File pubFile = new File("SystemKeys/gs-pubK.pem");
            File privFile = new File("SystemKeys/gs-privK.pem");
            if (!pubFile.exists() || !privFile.exists()) {
                System.out.println("Error: SystemKey's directory missing key/s");
                System.out.println("You must have both a private and public PEM key files to start");
                System.exit(0);
            } else {
                System.out.println("SystemKey's were successfully loaded.");
            }
        } else {
            System.out.println("\"SystemKeys\" directory was not found. Creating now...");
            sysDir.mkdir();
            System.out.println("Directory was successfully created!");
            System.out.println("Please load group server keys into this directory");
            System.exit(0);
        }

        // check if there is a UserPubKeys folder with atleast one priv and pub key pair
        File userDir = new File("UserPubKeys");
        if (userDir.exists()) {
            System.out.println("UserPubKeys directory was successfully found");
        } else {
            System.out.println("\"UserPubKeys\" directory was not found. Creating now...");
            userDir.mkdir();
            System.out.println("Directory was successfully created!");
            System.out.println("Please load all user's public keys into this directory");
            System.exit(0);
        }

        //This runs a thread that saves the lists on program exit
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new ShutDownListener(this));

        // try to open the groupKeyList, needed to maintain group keychains
        try {
            FileInputStream fis = new FileInputStream(keysFile);
            keyStream = new ObjectInputStream(fis);
            keyList = (GroupKeyList) keyStream.readObject();
            System.out.println("GroupKeyList File successfully loaded");
        } catch (FileNotFoundException e) {
            System.out.println("GroupKeyList File Does Not Exist. Creating GroupKeyList...");
			keyList = new GroupKeyList();
        } catch (IOException e) {
            System.out.println("Error reading from GroupKeyList file");
            System.exit(-1);
        } catch (ClassNotFoundException e) {
            System.out.println("Error reading from GroupKeyList file");
            System.exit(-1);
        }

        // try to open the groupList, needed to check or add first time ADMIN
        try {
            FileInputStream fis = new FileInputStream(groupFile);
            groupStream = new ObjectInputStream(fis);
            groupList = (GroupList) groupStream.readObject();
            System.out.println("GroupList File successfully loaded");
        } catch (FileNotFoundException e) {
            System.out.println("GroupList File Does Not Exist. Creating GroupList...");
            System.out.println("ADMIN Group Created For First Use...");

            //Create a new list, add current user to the ADMIN group. They now own the ADMIN group.
            groupList = new GroupList();
            groupList.addGroup("ADMIN");
            keyList.newGroupKeyChain("ADMIN");
        } catch (IOException e) {
            System.out.println("Error reading from GroupList file");
            System.exit(-1);
        } catch (ClassNotFoundException e) {
            System.out.println("Error reading from GroupList file");
            System.exit(-1);
        }

        //Open user file to get user list
        try {
            FileInputStream fis = new FileInputStream(userFile);
            userStream = new ObjectInputStream(fis);
            userList = (UserList) userStream.readObject();
            System.out.println("UserList File successfully loaded");
        } catch (FileNotFoundException e) {
            Scanner sc = new Scanner(System.in);
            System.out.println("UserList File Does Not Exist. Creating UserList...");
            System.out.println("No users currently exist. Your account will be the administrator.");
            System.out.print("Enter your username: ");
            String username = sc.next();

            //Create a new list, add current user to the ADMIN group. They now own the ADMIN group.
            userList = new UserList();
            userList.addUser(username);
            userList.addGroup(username, "ADMIN");
            userList.addOwnership(username, "ADMIN");
            groupList.addUser("ADMIN", username);
            groupList.addOwnership("ADMIN", username);
            userList.addUserStartBlock(username, "ADMIN", 0);
        } catch (IOException e) {
            System.out.println("Error reading from UserList file");
            System.exit(-1);
        } catch (ClassNotFoundException e) {
            System.out.println("Error reading from UserList file");
            System.exit(-1);
        }

        //Autosave Daemon. Saves lists every 5 minutes
        AutoSave aSave = new AutoSave(this);
        aSave.setDaemon(true);
        aSave.start();

        //This block listens for connections and creates threads on new connections
        try {

            final ServerSocket serverSock = new ServerSocket(port);

            Socket sock = null;
            GroupThread thread = null;
            System.out.println("Group Server up and running...");
            while (true) {
                sock = serverSock.accept();
                thread = new GroupThread(sock, this);
                thread.start();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }

    }

}

//This thread saves the user list
class ShutDownListener extends Thread {

    public GroupServer my_gs;

    public ShutDownListener(GroupServer _gs) {
        my_gs = _gs;
    }

    public void run() {
        System.out.println("Shutting down server");
        ObjectOutputStream outStream;
        try {
            outStream = new ObjectOutputStream(new FileOutputStream("UserList.bin"));
            outStream.writeObject(my_gs.userList);
            outStream.flush();
            outStream = new ObjectOutputStream(new FileOutputStream("GroupList.bin"));
            outStream.writeObject(my_gs.groupList);
            outStream.flush();
            outStream = new ObjectOutputStream(new FileOutputStream("GroupKeyList.bin"));
            outStream.writeObject(my_gs.keyList);
            outStream.flush();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}

class AutoSave extends Thread {

    public GroupServer my_gs;

    public AutoSave(GroupServer _gs) {
        my_gs = _gs;
    }

    public void run() {
        do {
            try {
                Thread.sleep(300000); //Save group and user lists every 5 mins
                System.out.println("Autosave group and user lists...");
                ObjectOutputStream outStream;
                try {
                    outStream = new ObjectOutputStream(new FileOutputStream("UserList.bin"));
                    outStream.writeObject(my_gs.userList);
                    outStream = new ObjectOutputStream(new FileOutputStream("GroupList.bin"));
                    outStream.writeObject(my_gs.groupList);
                    outStream = new ObjectOutputStream(new FileOutputStream("GroupKeyList.bin"));
                    outStream.writeObject(my_gs.keyList);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            } catch (Exception e) {
                System.out.println("Autosave Interrupted");
            }
        } while (true);
    }
}
