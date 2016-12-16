/*
	This class will test all possible getters and setters for a driver class
*/	
import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;
import org.mockito.*;
public class DriverTest
{
	@Before
	public void setUp() throws Exception 
	{
	
	}
	
	
	/*
		Initlization tests
	*/ 
	
	// Test that if no integer is passed as a constructor,
	// that the driver number returned will be -1
	@Test
	public void testDriverNumberEmpty()
	{
		Driver test = new Driver();
		int testInt = test.getDriverNum();
		assertTrue(testInt == -1);
	}
	// Current location should be null for a newly created driver
	@Test
	public void testCurrentLocationSetup()
	{
		Driver test = new Driver();
		assertNull(test.getLocation());
	}
	// Check the total number of coffee cups for a new driver is set to 0.
	@Test
	public void testCoffeeEmpty()
	{
		Driver test = new Driver();
		assertTrue(test.getCoffeeCups() == 0);
	}
	
	
	/*
		Tests for setting certain values and ensuring they are actually set.
	*/
	// Set the constructor be 1 and vefify driver number changed to 1
	@Test
	public void testDriverNumberSet()
	{
		Driver test = new Driver(1);
		int testInt = test.getDriverNum();
		assertTrue(testInt == 1);
	}
	// Change the current location of the new driver, verify the change was made
	@Test
	public void testLocationSet()
	{
		Driver test = new Driver(1);
		test.setLocation("TestingLand");
		assertEquals(test.getLocation(), "TestingLand");
	}
	// Testing that each time a the add coffee method is called, one is successfully added to total coffee count
	@Test
	public void testCoffeeAdding()
	{
		Driver test = new Driver();
		for(int i = 0; i < 10; i++)
			test.addCoffee();
		assertEquals(test.getCoffeeCups(), 10);
	}
}