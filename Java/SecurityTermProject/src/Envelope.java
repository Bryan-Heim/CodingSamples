
import java.util.ArrayList;

public class Envelope implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7726335089122193103L;
    private String msg;
    private ArrayList<Object> objContents = new ArrayList<Object>();
    private byte[] bytes1, bytes2, bytes3;
    private int counter;

    public Envelope(String text) {
        msg = text;
    }

    public String getMessage() {
        return msg;
    }

    public ArrayList<Object> getObjContents() {
        return objContents;
    }

    public int getCounter() {
        return counter;
    }

    public void addObject(Object object) {
        objContents.add(object);
    }

    public void addBytes1(byte[] bytes1ToAdd) {
        bytes1 = bytes1ToAdd;
    }

    public void addBytes2(byte[] bytes2ToAdd) {
        bytes2 = bytes2ToAdd;
    }

    public void addBytes3(byte[] bytes3ToAdd) {
        bytes3 = bytes3ToAdd;
    }

    public byte[] getBytes1() {
        return bytes1;
    }

    public byte[] getBytes2() {
        return bytes2;
    }

    public byte[] getBytes3() {
        return bytes3;
    }

    public void addCounter(int x) {
        counter = x;
    }

}
