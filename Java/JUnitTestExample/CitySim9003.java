import java.util.*;
/*
	Name: Bryan Heim
	Class: 1632 - Deliverable 2
	Description: This is the main launching point for the simulation application. This application meets the requirements
	as defined in the requirements.md presented. The program will take a random seed as a command line argument and will
	simulate 5 drivers, all driving in random sequences throughout a city with locations Hotel, Library, Coffee, or Diner
	The requirements can be found at: https://github.com/laboon/CS1632_Fall2016/blob/master/deliverables/2/requirements.md
*/
public class CitySim9003
{
	public static void main(String[] args)
	{
		// we only accept one argument in order to start
		if(args.length == 1)
		{
			try
			{
				// parse the seed
				int seed = Integer.parseInt(args[0]);
				
				// set-up the initial state of the simulation
				DriveSimulator simulate = new DriveSimulator(seed);
				
				// run the simulation for 5 drivers
				for(int i = 0; i < 5; i++)
					simulate.driveUntilOut(i+1);
			}
			// couldn't parse the seed
			catch(Exception e){System.out.println("The argument provided must have a valid integer value.");}
		}
		else
			System.out.println("Error on start-up. You must start the program with an integer seed as the first argument.");

	}
}