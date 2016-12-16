/*
	This class will test all public methods for RandomLocationTest.java
*/	
import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;
import org.mockito.*;
public class RandomLocationTest
{
	@Before
	public void setUp() throws Exception 
	{	
	
	}

	/*
		First ensure that if we ask for starting locations, we never get "Outside" using a fixed seeds 1, 2, 3, and 4
	*/
	
	// seed 1 test
	@Test
	public void testStartLocationSeed1()
	{
		RandomLocation test = new RandomLocation(1);
		String start = test.getRandomStartLocation();
		assertFalse(start.equals("Outside"));
	}
	// seed 2 test
	@Test
	public void testStartLocationSeed2()
	{
		RandomLocation test = new RandomLocation(2);
		String start = test.getRandomStartLocation();
		assertFalse(start.equals("Outside"));
	}
	// seed 3 test
	@Test
	public void testStartLocationSeed3()
	{
		RandomLocation test = new RandomLocation(3);
		String start = test.getRandomStartLocation();
		assertFalse(start.equals("Outside"));
	}
	// seed 4 test
	@Test
	public void testStartLocationSeed4()
	{
		RandomLocation test = new RandomLocation(4);
		String start = test.getRandomStartLocation();
		assertFalse(start.equals("Outside"));
	}

	/*
		For each of the possible starting locations, check that the getNextLocation function will always return something different
		Each test will use the same seed for consistency
	*/
	
	//Using Hotel as the currentLocation
	@Test
	public void testNextLocationFromHotel()
	{
		RandomLocation test = new RandomLocation(1);
		String nextLocation = test.getNextLocation("Hotel");
		assertFalse(nextLocation.equals("Hotel"));
	}
	//Using the Diner as the currentLocation
	@Test
	public void testNextLocationFromDiner()
	{
		RandomLocation test = new RandomLocation(1);
		String nextLocation = test.getNextLocation("Diner");
		assertFalse(nextLocation.equals("Diner"));
	}
	//Using the Library as the currentLocation
	@Test
	public void testNextLocationFromLibrary()
	{
		RandomLocation test = new RandomLocation(1);
		String nextLocation = test.getNextLocation("Library");
		assertFalse(nextLocation.equals("Library"));
	}
	//Using Coffee as the currentLocation
	@Test
	public void testNextLocationFromCoffee()
	{
		RandomLocation test = new RandomLocation(1);
		String nextLocation = test.getNextLocation("Coffee");
		assertFalse(nextLocation.equals("Coffee"));
	}
	
	/*
		Running the same test as above just using a different seed to ensure the results are still valid.
	*/
		//Using Hotel as the currentLocation
	@Test
	public void testNextLocationFromHotel2()
	{
		RandomLocation test = new RandomLocation(2);
		String nextLocation = test.getNextLocation("Hotel");
		assertFalse(nextLocation.equals("Hotel"));
	}
	//Using the Diner as the currentLocation
	@Test
	public void testNextLocationFromDiner2()
	{
		RandomLocation test = new RandomLocation(2);
		String nextLocation = test.getNextLocation("Diner");
		assertFalse(nextLocation.equals("Diner"));
	}
	//Using the Library as the currentLocation
	@Test
	public void testNextLocationFromLibrary2()
	{
		RandomLocation test = new RandomLocation(2);
		String nextLocation = test.getNextLocation("Library");
		assertFalse(nextLocation.equals("Library"));
	}
	//Using Coffee as the currentLocation
	@Test
	public void testNextLocationFromCoffee2()
	{
		RandomLocation test = new RandomLocation(2);
		String nextLocation = test.getNextLocation("Coffee");
		assertFalse(nextLocation.equals("Coffee"));
	}

}