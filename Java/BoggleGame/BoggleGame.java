import java.io.*;
import java.util.*;

public class BoggleGame
{
	public static void main(String[] args) throws Exception
	{
		String[][] board = loadBoard( args[0] );
		ArrayList<String> dictionary = loadDictionary();
		Collections.sort(dictionary);
		/*for ( int u = 0; u < dictionary.size(); u++)
			System.out.println(dictionary.get(u));						// used to test if dictionary loaded
		System.out.println("# of Words: " + dictionary.size());*/
		int boardSize = board.length; 
		boolean visited[][] = new boolean[boardSize][boardSize];
		for(int r = 0; r < boardSize ; r++)								//set all visited positions to false
        	for(int c = 0; c < boardSize; c++)
                 visited[r][c] = false;
		//printBoard("\n   Your Boggle Board:\n", board );  used to test that board loaded
		BoggleSolver bs = new BoggleSolver(boardSize, visited, board, dictionary);
		bs.findAllStrings();
		
	}

	private static String[][] loadBoard( String infileName) throws Exception
    {
        Scanner infile = new Scanner( new File(infileName) );
        int rows=infile.nextInt();
        int cols = rows;  		// ASSUME A SQUARE GRID
        String[][] board = new String[rows][cols];
        for(int r = 0; r < rows ; r++)
        	for(int c = 0; c < cols; c++)
                 board[r][c] = infile.next();

        infile.close();
        return board;
    }
	private static ArrayList<String> loadDictionary()
	{
		BufferedReader  dictionaryFile = null;
		ArrayList<String> list = new ArrayList<String>();
		try 
		{
			dictionaryFile = new BufferedReader( new FileReader("dictionary.txt" ) );
			try{
				    String line = "";  
					while((line = br.readLine()) != null)
					{  
					  list.add(dictionaryFile.readLine()); 
					  line++;
					}  
			}
			catch (IOException e){
				System.out.println("File not Found");
			}
		}
		catch (FileNotFoundException e){
			System.out.println("File not Found");
		}
		return list;

	}
	/*private static void printBoard(String label, String[][] board )
    {
        System.out.println( label );
        System.out.print("   ");
        System.out.print( "\n   ");
        for(int c = 0; c < board.length; c++)
        	System.out.print("- ");
       System.out.print( "\n");

        for(int r = 0; r < board.length; r++)
        {	System.out.print("| ");								Used to test that board loaded
            for(int c = 0; c < board[r].length; c++)
                 System.out.print( board[r][c] + " ");
            System.out.println("|");
        }
        System.out.print( "   ");
        for(int c = 0; c < board.length; c++)
        	System.out.print("- ");
       System.out.print( "\n");
    }*/
}
class BoggleSolver
{
	int boardSize;
	boolean[][] visited;
	String[][] board;
	ArrayList<String> dictionary;
	static ArrayList<String> returnList = new ArrayList<String>();
	public BoggleSolver(int boardSizeIn, boolean[][] visitedIn, String[][] boardIn, ArrayList<String> dictionaryIn)
	{
		boardSize = boardSizeIn;
		visited = visitedIn;
		board = boardIn;
		dictionary = dictionaryIn;
	}
	public void findAllStrings()
	{	
		for (int r = 0; r < boardSize; r++)
            for (int c = 0; c < boardSize; c++)
                depthFirstSearch(r,c,"");
		// uses a hashset temporarily to store data
		HashSet hs = new HashSet();
		// moved returnList because Hashset doesnt allow repeats
		hs.addAll(returnList);
		returnList.clear();
		// refill the arraylist without duplicate entries
		returnList.addAll(hs);
		Collections.sort(returnList);
		for (int YES = 0; YES < returnList.size(); YES++)
			System.out.println(returnList.get(YES));
	}
	private void depthFirstSearch(int r, int c, String s)
	{
		if (r < 0 || c < 0 || r >= boardSize || c >= boardSize)
			return;
		if (visited[r][c]) 
			return;
		// if prefix is NOT part of a word in dictionary, return, this is what saves time
		if (!containsDictionaryPrefix(s))
			return;
		visited[r][c] = true;
		s = s + board[r][c];
		 //recursively check neighbours
		for (int row = -1; row <= 1; row++)
			for (int col = -1; col <= 1; col++)
				depthFirstSearch(r + row, c + col, s);
		visited[r][c] = false;
	}
	private boolean binarySearchForWord(String s)
	{
		int start = 0, end = dictionary.size();
				
		while(end >= start) 
		{
			int middle = (start + end) / 2;
			if(dictionary.get(middle).equals(s)) 
				return true;
			if(dictionary.get(middle).compareTo(s) < 0) 
				start = middle + 1;
			if(dictionary.get(middle).compareTo(s) > 0) 
				end = middle - 1;
		}
		return false;
	}
	private boolean containsDictionaryPrefix(String s)
	{
		int start = 0, end = dictionary.size();
				
		while(end >= start) 
		{
			int middle = (start + end) / 2;
			if(dictionary.get(middle).startsWith(s))
			{
				if(s.length() > 2)
				{
					if (binarySearchForWord(s))   
						returnList.add(s);
				}
				return true;
			}
			if(dictionary.get(middle).compareTo(s) < 0) 
				start = middle + 1;
			if(dictionary.get(middle).compareTo(s) > 0) 
				end = middle - 1;
		}
		s = s.substring(0,s.length()-2);
		return false;
	}

}