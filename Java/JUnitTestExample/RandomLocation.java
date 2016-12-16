import java.util.*;
/*
	This class will handle the random direction selection for a driver and which random location they start at
*/
public class RandomLocation
{
	Random random;
	// all possible locations as per the requirements
	String[] theLocs = {"Hotel", "Diner", "Library", "Coffee", "Outside"};
	public RandomLocation(int seed)
	{
		random = new Random(seed);
	}
	
	// get a random number between 0 and daMax
	private int getRandomNumber(int daMax)
	{
		return random.nextInt(daMax);
	}
	
	// get a random index from 0 to 3 to pick a starting location
	public String getRandomStartLocation()
	{
		return theLocs[getRandomNumber(4)];
	}
	
	// check where driver is now, get a valid, random location to move to next
	public String getNextLocation(String currentLocation)
	{
		String tempLocation = "";
		RoadConnections checkIfValid = new RoadConnections();
		// if our next location is the same as where we are keep searching, or if you cant get from current to next in one road
		do{
			tempLocation = theLocs[getRandomNumber(5)];
		}while(tempLocation == currentLocation || checkIfValid.roadCombo(currentLocation,tempLocation) == false);
		// we have a valid location, return it
		return tempLocation;
	}
}