
import java.util.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.KeyGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class GroupKeyList implements java.io.Serializable {

    private static final long serialVersionUID = 7600343803563417992L;
    private Hashtable<String, KeyChain> table = new Hashtable<String, KeyChain>();

    // create a place for new group in the table and make their keychain with node 0
    public synchronized void newGroupKeyChain(String groupName) {
        KeyChain daChain = new KeyChain();
        table.put(groupName, daChain);
    }

    // get all the keys a group has used before
    public synchronized ArrayList<SecretKey> getGroupsKeyEncChain(String groupName) {
        return table.get(groupName).getEncKeys();
    }
	
	public synchronized ArrayList<SecretKey> getGroupsKeyIntChain(String groupName) {
        return table.get(groupName).getIntKeys();
    }

    // add a new to key to that groups key chain
    public synchronized void addKeysToGroup(String groupName) {
        table.get(groupName).addKeys();
    }

    public synchronized int getCurBlockNum(String groupName) {
        return ((table.get(groupName).getEncKeys().size()) - 1);
    }

    class KeyChain implements java.io.Serializable {

        private static final long serialVersionUID = -3862752957243865624L;
        private ArrayList<SecretKey> keyEncList;
		private ArrayList<SecretKey> keyIntList;

        public KeyChain() {
            keyEncList = new ArrayList<SecretKey>();
			keyIntList = new ArrayList<SecretKey>();
            addKeys(); // make first key
        }

        public synchronized ArrayList<SecretKey> getEncKeys() {
            return keyEncList;
        }
		
		public synchronized ArrayList<SecretKey> getIntKeys() {
            return keyIntList;
        }

        public synchronized void addKeys() {
            SecretKey newEncKey = generateKey();
			SecretKey newIntKey = generateKey();
			System.out.println("new keys being made");
            keyEncList.add(newEncKey);
			keyIntList.add(newIntKey);
			System.out.println("new keys added");
        }

        private synchronized SecretKey generateKey() {
            try {
                Security.addProvider(new BouncyCastleProvider());
                KeyGenerator theGenerator = KeyGenerator.getInstance("AES", "BC");
                theGenerator.init(256, new SecureRandom());
                SecretKey madeKey = theGenerator.generateKey();
                return madeKey;
            } catch (NoSuchAlgorithmException e) {
                return null;
            } catch (NoSuchProviderException e) {
                return null;
            }
        }
    }
}
