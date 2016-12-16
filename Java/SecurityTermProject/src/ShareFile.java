
import java.util.ArrayList;


public class ShareFile implements java.io.Serializable, Comparable<ShareFile> {

    /**
     *
     */
    private static final long serialVersionUID = -6699986336399821598L;
    private String group;
    private String path;
    private String owner;
    private ArrayList<String> IVList;
    private int keyNumber;
    private String HMAC;

    public ShareFile(String _owner, String _group, String _path, int keyNumber, ArrayList<String> IVList, String HMAC) {
        group = _group;
        owner = _owner;
        path = _path;
        this.IVList = IVList;
        this.keyNumber = keyNumber;
        this.HMAC = HMAC;
    }

    public String getPath() {
        return path;
    }

    public String getOwner() {
        return owner;
    }

    public String getGroup() {
        return group;
    }
    
    public int getKeyNumber() {
        return keyNumber;
    }
    
    public ArrayList<String> getIVList() {
        return IVList;
    }

    public String getHMAC() {
        return HMAC;
    }

    public int compareTo(ShareFile rhs) {
        if (path.compareTo(rhs.getPath()) == 0) {
            return 0;
        } else if (path.compareTo(rhs.getPath()) < 0) {
            return -1;
        } else {
            return 1;
        }
    }

}
