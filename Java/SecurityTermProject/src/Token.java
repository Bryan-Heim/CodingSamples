
import java.util.*;

public class Token implements UserToken, java.io.Serializable {

    // define the variables that will be used by the whole class

    private String issuer = "", subject = "", ipAddr = "", portNum = "";
    List<String> groups = null;
    byte[] signedHash;

    // initialize the variables from the constructor when user tokens are created
    public Token(String issuerIn, String subjectIn, List<String> groupsIn, String ipIn, String portIn, byte[] signedHashIn) {
        issuer = issuerIn;
        subject = subjectIn;
        groups = groupsIn;
        ipAddr = ipIn;
        portNum = portIn;
        signedHash = signedHashIn;
    }

    // implement the getters as specified in the UserToken.java interface
    public String getIssuer() {
        return issuer;
    } // issuer = server who gave token

    public String getSubject() {
        return subject;
    } // subject = who requested the token

    public List<String> getGroups() {
        return groups;
    } // what groups that subject is in

    public String getIPAddress() {
        return ipAddr;
    }

    public String getPortNumber() {
        return portNum;
    }

    public byte[] getSignedHash() {
        return signedHash;
    } // what groups that subject is in
}
