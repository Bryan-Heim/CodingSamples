/*
	This class will be the one actually responsible for launching each test, and determining if they passed or failed.
*/
import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.runner.*;
import org.junit.runner.notification.*;
public class TestRunner 
{
    public static void main(String[] args) 
	{

		ArrayList<Class> classesToTest = new ArrayList<Class>();
		boolean anyFailures = false;
		
		// add the test classes
		classesToTest.add(DriverTest.class);
		classesToTest.add(RandomLocationTest.class);
		classesToTest.add(RoadConnectionsTest.class);
		
		// For all test classes added, loop through and use JUnit to run
		for (Class c: classesToTest) 
		{
			Result r = JUnitCore.runClasses(c);

			// Print out any failures for this class.
			for (Failure f : r.getFailures()) 
			{
				System.out.println(f.toString());
			}

			// If r is not successful, there was at least one
			// failure.  Thus, set anyFailures to true - this
			// can never be set back to false (no amount of
			// successes will ever eclipse the fact that there
			// was at least one failure.
			if (!r.wasSuccessful()) 
			{
				anyFailures = true;
			}
		}
		if (anyFailures) 
		{
			System.out.println("\nError! There was at least one failure, read above to find out why.");
		} 
		else 
		{
			System.out.println("\nAll Tests passed successfully without error.");
		} 
    }
} 
