/*
	This class will test all possible getters and setters for a driver class
*/	
import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;
import org.mockito.*;
public class RoadConnectionsTest
{
	@Before
	public void setUp() throws Exception 
	{

	}

	/*
		testValidConnections will test to ensure that if two locations are connected
		by a road, then we should return true, otherwise return false. The first four
		(testValidConnections1-4) will check for starting points to valid nexts.
		The next two test will consist of impossible combinations that should return false
	*/
	@Test
	public void testValidConnections1()
	{
		RoadConnections test = new RoadConnections();
		boolean valid = test.roadCombo("Diner","Coffee");
		assertTrue(valid);
	}
	@Test
	public void testValidConnections2()
	{	
		RoadConnections test = new RoadConnections();
		boolean valid = test.roadCombo("Hotel","Diner");
		assertTrue(valid);
	}
	@Test
	public void testValidConnections3()
	{
		RoadConnections test = new RoadConnections();
		boolean valid = test.roadCombo("Library","Hotel");
		assertTrue(valid);
	}
	@Test
	public void testValidConnections4()
	{
		RoadConnections test = new RoadConnections();
		boolean valid = test.roadCombo("Coffee","Diner");
		assertTrue(valid);
	}
	@Test
	public void testValidConnections5()
	{
		RoadConnections test = new RoadConnections();
		boolean valid = test.roadCombo("Coffee","Hotel");
		assertFalse(valid);
	}
	@Test
	public void testValidConnections6()
	{
		RoadConnections test = new RoadConnections();
		boolean valid = test.roadCombo("Library","Diner");
		assertFalse(valid);
	}

	// Now we can setup 4 different test to ensure that we
	// can get the street name given different combinations.
	// Once again, 1-4 represnt starting at each different possible start location.
	public void getStreetName1()
	{
		RoadConnections test = new RoadConnections();
		String street = test.getStreetName("Hotel","Diner");
		assertEquals(street, "Forth Ave.");
	}
	public void getStreetName2()
	{
		RoadConnections test = new RoadConnections();
		String street = test.getStreetName("Diner","Coffee");
		assertEquals(street, "Phil St.");
	}
	public void getStreetName3()
	{
		RoadConnections test = new RoadConnections();
		String street = test.getStreetName("Library","Hotel");
		assertEquals(street, "Bill St.");
	}
	public void getStreetName4()
	{
		RoadConnections test = new RoadConnections();
		String street = test.getStreetName("Coffee","Library");
		assertEquals(street, "Fifth Ave.");
	}
}