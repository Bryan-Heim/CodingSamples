
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter; // for logging
import java.io.StringWriter;

public abstract class Client {

    // for logging errors
    StringWriter stackTraceString = new StringWriter();
    PrintWriter stackTrace = new PrintWriter(stackTraceString);

    /* protected keyword is like private but subclasses have access
     * Socket and input/output streams
     */
    protected Socket sock;
    protected ObjectOutputStream output;
    protected ObjectInputStream input;
    protected DataLogger log; // defined by either file or group

    public boolean connect(final String server, final int port) {
        System.out.println("Attempting to connect to the server...");

        // based on sample code given in EchoClient.java given to us!
        try {
            // try to connect to the server over the given port
            sock = new Socket(server, port);
            output = new ObjectOutputStream(sock.getOutputStream());
            input = new ObjectInputStream(sock.getInputStream());
            System.out.println("Successfully connected to " + server + " over port #" + port + ".");
            // we can return true because were ready to start using the streams
            return true;
        } catch (Exception e) {
            System.out.println("Failed to connect to the server.");
            e.printStackTrace(new PrintWriter(stackTrace));
            log.write("err", "(Client/connect) " + e.getMessage(), stackTraceString.toString());
            return false;
        }
    }

    public boolean isConnected() {
        if (sock == null || !sock.isConnected()) {
            return false;
        } else {
            return true;
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                Envelope message = new Envelope("DISCONNECT");
                output.writeObject(message);
            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(stackTrace));
                log.write("err", "(Client/disconnect) " + e.getMessage(), stackTraceString.toString());
            }
        }
    }
}
