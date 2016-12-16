/* This list represents the groups on the server */

import java.util.*;

public class GroupList implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionGID = 2096945827804349723L;
    private Hashtable<String, Group> list = new Hashtable<String, Group>();

    public synchronized void addGroup(String group) {
        Group newGroup = new Group();
        list.put(group, newGroup);
    }

    public synchronized void deleteGroup(String group) {
        list.remove(group);
    }

    public synchronized boolean checkGroup(String group) {
        if (list.containsKey(group)) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized ArrayList<String> getGroupsUsers(String group) {
        return list.get(group).getUsers();
    }

    public synchronized ArrayList<String> getGroupOwnership(String group) {
        return list.get(group).getOwnership();
    }

    public synchronized void addUser(String group, String username) {
        list.get(group).addUser(username);
    }

    public synchronized void removeUser(String group, String username) {
        list.get(group).removeUser(username);
    }

    public synchronized void addOwnership(String group, String username) {
        list.get(group).addOwnership(username);
    }

    public synchronized void removeOwnership(String group, String username) {
        list.get(group).removeOwnership(username);
    }

    class Group implements java.io.Serializable {

        /**
         *
         */
        private static final long serialVersionUID = -5820934702457245309L;
        private ArrayList<String> users;
        private ArrayList<String> ownership;

        public Group() {
            users = new ArrayList<String>();
            ownership = new ArrayList<String>();
        }

        public ArrayList<String> getUsers() {
            return users;
        }

        public ArrayList<String> getOwnership() {
            return ownership;
        }

        public void addUser(String user) {
            users.add(user);
        }

        public void removeUser(String user) {
            if (!users.isEmpty()) {
                if (users.contains(user)) {
                    users.remove(users.indexOf(user));
                }
            }
        }

        public void addOwnership(String user) {
            ownership.add(user);
        }

        public void removeOwnership(String user) {
            if (!ownership.isEmpty()) {
                if (ownership.contains(user)) {
                    ownership.remove(ownership.indexOf(user));
                }
            }
        }

    }

}
