/*
	A driver will have a number (i.e. Driver 3), a current location in the City, and a coffee cup count
	We will be able to change all of these properties via setter methods provided below.
*/	
public class Driver
{
	int driverNumber, totCoff = 0;
	String location = null;
	public Driver()
	{
		driverNumber = -1;
	}
	public Driver(int num)
	{
		driverNumber = num;
	}
	
	// to uniquely identify drivers
	public int getDriverNum()
	{
		return driverNumber;
	}
	
	// get/set current location of the driver
	public String getLocation()
	{
		return location;
	}
	public void setLocation(String toSet)
	{
		location = toSet;
		return;
	}
	
	// get/set total times stopped for coffee
	public int getCoffeeCups()
	{
		return totCoff;
	}
	public void addCoffee()
	{
		totCoff++;
	}
}