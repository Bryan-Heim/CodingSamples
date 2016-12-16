
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat; // for time stampins
/*
 A class to be used for logging both errors and activity on the Group, File, and Application client interfaces
 */

public class DataLogger {

    private String type, timeStamp;
    private int loggedInit = 0;

    // init and ensure the files are there to write to the logs too
    public DataLogger(String typeIn) {
        // will either be Group, File, Application or Develop(must say which class in method)
        type = typeIn;
        // for Error or Data
        String label = "";
        // for making files if needed
        File holder;

        // create the error and data log for each type if it doesn't exist
        String[] types = {"Group", "File", "Application", "Develop", "Group", "File", "Application"};
        try {
            // check if directory exists first
            File directory = new File("logs");
            if (!directory.exists()) {
                directory.mkdir();
            }

            for (int i = 0; i < 7; i++) {
                if (i < 4) {
                    label = "Error";
                } else {
                    label = "Data";
                }
                // if file doesn't exist make it!
                if (!(new File("logs/" + type + label + "Log.txt").exists())) {
                    holder = new File("logs/" + types[i] + label + "Log.txt");
                    holder.createNewFile();
                }
                loggedInit = 1;
            }
        } // if something doesn't work, use getSetupDone() to know in calling class
        catch (Exception e) {
        }
    }

    public synchronized void write(String errorOrData, String message, String messageST) {
        String builtMessage = "", fileName = "";
        timeStamp = new SimpleDateFormat("d/MMM/yyyy HH:mm:ss").format(new Date());

        if (errorOrData.equals("err")) {
            // check where error came from and place into correct error log
            if (type.equals("Group")) {
                fileName = "logs/GroupErrorLog.txt";
            } else if (type.equals("File")) {
                fileName = "logs/FileErrorLog.txt";
            } else if (type.equals("Application")) {
                fileName = "logs/ApplicationErrorLog.txt";
            } else {
                fileName = "logs/DevelopErrorLog.txt";
            }

            // create errorLog message and send to the append method
            builtMessage = timeStamp + "-ERROR: " + message + System.getProperty("line.separator") + "Stack Trace: " + messageST + System.getProperty("line.separator");
            appendMessage(fileName, builtMessage);
        } else if (errorOrData.equals("data")) {
            // check where error came from and place into correct error log
            if (type.equals("Group")) {
                fileName = "logs/GroupDataLog.txt";
            } else if (type.equals("File")) {
                fileName = "logs/FileDataLog.txt";
            } else {
                fileName = "logs/ApplicationDataLog.txt";
            }

            // create the activity message and append
            builtMessage = timeStamp + "-Data Recorded: " + message + System.getProperty("line.separator");
            appendMessage(fileName, builtMessage);
        }
        return;
    }

    public synchronized void appendMessage(String fileName, String message) {
        // try to open the file and append the appropriate message
        PrintWriter daPrinter = null;
        try {
            FileOutputStream logFileAppender = new FileOutputStream(new File(fileName), true);
            daPrinter = new PrintWriter(logFileAppender);
            daPrinter.println(message);
            daPrinter.close();
        } // TODO should catch this, make log for log? 
        catch (Exception e) {
        } finally {
            return;
        }
    }
}
