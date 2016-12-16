
public class LogTester {

    public static void main(String[] args) {
        DataLogger gLog = new DataLogger("Group");
        DataLogger fLog = new DataLogger("File");
        DataLogger aLog = new DataLogger("Application");
        DataLogger dLog = new DataLogger("Develop");
        int i;

        // test each log with 5 messages
        for (i = 0; i < 5; i++) {
            gLog.write("err", "JunkError" + i, "Stack1" + System.getProperty("line.separator") + "Stack2" + System.getProperty("line.separator") + "Stack3" + System.getProperty("line.separator") + "Stack4" + System.getProperty("line.separator"));
        }

        for (i = 0; i < 5; i++) {
            fLog.write("err", "JunkError" + i, "Stack1" + System.getProperty("line.separator") + "Stack2" + System.getProperty("line.separator") + "Stack3" + System.getProperty("line.separator") + "Stack4" + System.getProperty("line.separator"));
        }

        for (i = 0; i < 5; i++) {
            aLog.write("err", "JunkError" + i, "Stack1" + System.getProperty("line.separator") + "Stack2" + System.getProperty("line.separator") + "Stack3" + System.getProperty("line.separator") + "Stack4" + System.getProperty("line.separator"));
        }

        for (i = 0; i < 5; i++) {
            dLog.write("err", "JunkError" + i, "Stack1" + System.getProperty("line.separator") + "Stack2" + System.getProperty("line.separator") + "Stack3" + System.getProperty("line.separator") + "Stack4" + System.getProperty("line.separator"));
        }

        for (i = 0; i < 5; i++) {
            gLog.write("data", "Junksaohgkjsa;lhal;fkhlakflhka'lfdkhl" + i, "");
        }

        for (i = 0; i < 5; i++) {
            fLog.write("data", "JunkEsadjkhja;sfjhl;a" + i, "");
        }

        for (i = 0; i < 5; i++) {
            aLog.write("data", "Junkasjgkajsfk;hjl;adfs" + i, "");
        }

        System.out.println("\nAll Done!");
    }
}
