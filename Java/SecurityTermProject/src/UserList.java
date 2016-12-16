/* This list represents the users on the server */

import java.util.*;

public class UserList implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7600343803563417992L;
    private Hashtable<String, User> list = new Hashtable<String, User>();

    public synchronized void addUser(String username) {
        User newUser = new User();
        list.put(username, newUser);
    }

    public synchronized void deleteUser(String username) {
        list.remove(username);
    }

    public synchronized boolean checkUser(String username) {
        if (list.containsKey(username)) {
            return true;
        } else {
            return false;
        }
    }

		//definitely add GroupList.. WIP...
    public synchronized ArrayList<String> getUserGroups(String username) {
        return list.get(username).getGroups();
    }

    public synchronized ArrayList<String> getUserOwnership(String username) {
        return list.get(username).getOwnership();
    }

    public synchronized void addGroup(String user, String groupname) {
        list.get(user).addGroup(groupname);
    }

    public synchronized void removeGroup(String user, String groupname) {
        list.get(user).removeGroup(groupname);
    }

    public synchronized void addOwnership(String user, String groupname) {
        list.get(user).addOwnership(groupname);
    }

    public synchronized void removeOwnership(String user, String groupname) {
        list.get(user).removeOwnership(groupname);
    }

    public synchronized void addUserStartBlock(String user, String groupName, int blockNum) {
        list.get(user).addStartBlock(groupName, blockNum);
    }

    public synchronized void addUserEndBlock(String user, String groupName, int blockNum) {
        list.get(user).addEndBlock(groupName, blockNum);
    }

    public synchronized int getUserStartBlock(String user, String groupName) {
        return list.get(user).getStartBlock(groupName);
    }

    public synchronized int getUserEndBlock(String user, String groupName) {
        return list.get(user).getEndBlock(groupName);
    }

    public synchronized boolean isUserCurrentlyActive(String user, String groupName) {
        if (list.get(user).groupEndBlocks.get(groupName) != null) {
            return false;
        } else {
            return true; // they dont have an end block
        }
    }

    class User implements java.io.Serializable {

        /**
         *
         */
        private static final long serialVersionUID = -6699986336399821598L;
        private ArrayList<String> groups;
        private ArrayList<String> ownership;
        private HashMap<String, Integer> groupStartBlocks = new HashMap<String, Integer>();
        private HashMap<String, Integer> groupEndBlocks = new HashMap<String, Integer>();

        public User() {
            groups = new ArrayList<String>();
            ownership = new ArrayList<String>();
        }

        public ArrayList<String> getGroups() {
            return groups;
        }

        public ArrayList<String> getOwnership() {
            return ownership;
        }

        public void addGroup(String group) {
            groups.add(group);
        }

        public void removeGroup(String group) {
            if (!groups.isEmpty()) {
                if (groups.contains(group)) {
                    groups.remove(groups.indexOf(group));
                }
            }
        }

        public void addOwnership(String group) {
            ownership.add(group);
        }

        public void removeOwnership(String group) {
            if (!ownership.isEmpty()) {
                if (ownership.contains(group)) {
                    ownership.remove(ownership.indexOf(group));
                }
            }
        }

        public void addStartBlock(String groupName, int blockNum) {
            groupStartBlocks.put(groupName, blockNum);
        }

        public int getStartBlock(String groupName) {
            return groupStartBlocks.get(groupName);
        }

        public void addEndBlock(String groupName, int blockNum) {
            groupEndBlocks.put(groupName, blockNum);
        }

        public int getEndBlock(String groupName) {
            return groupEndBlocks.get(groupName);
        }

    }

}
