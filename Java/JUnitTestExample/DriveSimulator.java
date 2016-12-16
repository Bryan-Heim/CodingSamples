/*
	This class does the actual work of the simulation, creates drivers and simulates their drive through the city.
*/
public class DriveSimulator
{
	RandomLocation rL;
	RoadConnections theMap = new RoadConnections();
	public DriveSimulator(int seed)
	{
		// init the RandomLocation class using the given seed
		rL = new RandomLocation(seed);
	}
	
	// simulate the moves the drive does until they are out
	public void driveUntilOut(int driverNo)
	{
		// make a driver object,
		Driver testDriver = new Driver(driverNo);
		String curLoc = "", newLoc = "", streetName;
		// set the drivers starting location, and get a next location
		curLoc = rL.getRandomStartLocation();
		testDriver.setLocation(curLoc);
		newLoc = rL.getNextLocation(curLoc);
		
		// if the next location isnt immediately outside the city, iterate again
		while(!newLoc.equals("Outside"))
		{
			// if they landed on coffee, add a cup of coffee
			if(curLoc.equals("Coffee"))
				testDriver.addCoffee();
			// get the street name that connects the current location to the next
			streetName = theMap.getStreetName(curLoc,newLoc);
			// print out what the driver did!
			System.out.println("Driver " + driverNo + " heading from " + curLoc + " to " + newLoc + " via " + streetName);
			// they are now at the new location
			testDriver.setLocation(newLoc);
			curLoc = newLoc;
			newLoc = rL.getNextLocation(curLoc);
		}
		
		// when we are here, newLoc is equal to outside and they are leaving!
		streetName = theMap.getStreetName(curLoc, newLoc);
		System.out.println("Driver "+ driverNo +" heading from "+curLoc+" to "+newLoc+" City via " + streetName);
		// print which city they left too
		theMap.printLeavingCity(testDriver.getDriverNum(),curLoc);
		// finally print the total number of coffees obtained
		System.out.println("Driver " + driverNo + " got " + testDriver.getCoffeeCups() + " cup(s) of coffee.");
		System.out.println("-----");
		return;
	}
}