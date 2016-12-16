import java.util.*;

/*
	This class stores all information pertaining to road names, whether you can get from A to B in one iteration,
	and finally which city the driver left to when the leave the city.
*/
public class RoadConnections
{
	// check if there is a direct road from one to other, if return true
	public boolean roadCombo(String currentLocation, String possibleNext)
	{
		boolean connected = false;
		
		// check if you can go to the next location, given a location you will always have 2 possible choices
		if(currentLocation.equals("Hotel") && (possibleNext.equals("Diner") || possibleNext.equals("Library")))
			connected = true;
		else if(currentLocation.equals("Diner") && (possibleNext.equals("Coffee") || possibleNext.equals("Outside")))
			connected = true;
		else if(currentLocation.equals("Coffee") && (possibleNext.equals("Diner") || possibleNext.equals("Library")))
			connected = true;
		else if(currentLocation.equals("Library") && (possibleNext.equals("Hotel") || possibleNext.equals("Outside")))
			connected = true;
		
		return connected;
	}
	
	// get the name of the street that connects the old location to the new location
	public String getStreetName(String oldLocation, String newLocation)
	{
		String streetName = "";
		
		// get the streetName that was used to get from the old location to new location
		if((oldLocation.equals("Hotel") && newLocation.equals("Diner")) || (oldLocation.equals("Diner") && newLocation.equals("Outside")))
			streetName = "Forth Ave.";
		else if((oldLocation.equals("Hotel") && newLocation.equals("Library")) || (oldLocation.equals("Library") && newLocation.equals("Hotel")))
			streetName = "Bill St.";
		else if((oldLocation.equals("Diner") && newLocation.equals("Coffee")) || (oldLocation.equals("Coffee") && newLocation.equals("Diner")))
			streetName = "Phil St.";
		else if((oldLocation.equals("Coffee") && newLocation.equals("Library")) || (oldLocation.equals("Library") && newLocation.equals("Outside")))
			streetName = "Fifth Ave.";
	
		return streetName;
	}
	
	public void printLeavingCity(int driveNo, String currentLocation)
	{
		// when they leave the city, check the last place they were at to get the city they are heading too
		if(currentLocation.equals("Library"))
			System.out.println("Driver " + driveNo + " has gone to Cleveland!");
		else if(currentLocation.equals("Diner"))
			System.out.println("Driver " + driveNo + " has gone to Philadelphia!");
		return;
	}
}