/*
	An LZW compression algorithm implementation using variable bit lengths and two different modes for a full codebook
	Originally based off of LZW.java from http://algs4.cs.princeton.edu/55compression/LZW.java.html
	To compress a file use: java LZW - n < input.file   (replace "n" with "r" for reset mode)
	To decompress the file: java LZW + < input.file
	By Bryan Heim
*/

public class MyLZW {
    // number of input chars
    private static final int R = 256;
	// number of codewords = 2^W, starts at 2^9 but will increase when out of codewords
    private static int L = 512;
	// not final, starts at 9 and can vary up to 16 bits
    private static int W = 9;

    public static void compress(int mode) { 
		// mode meaning: 0 = do nothing, 1=reset
		// will set the first 2 bits so decompression knows which mode
		if( mode == 1) {
			BinaryStdOut.write(1, 2);
		}
		else {
			BinaryStdOut.write(0, 2); 
		}
		
		// read in the data
        String input = BinaryStdIn.readString(); 
        TST<Integer> st = new TST<Integer>();
		
		// fills codebook with the initial 256 ASCII codes
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;// R is codeword for EOF
		boolean mFlag = false;
		double sizeIn = (double)input.length();
		double sizeOut = 256; // because the codebook contains all 256 ascii values
		double ratioOfRatios = 0, oldRatio = 0, newRatio = 0;
		
        while (input.length() > 0) {
			// Find max prefix match s.
            String s = st.longestPrefixOf(input);
			//System.err.println("st.get(s) = " + st.get(s));
            BinaryStdOut.write(st.get(s), W);
            int t = s.length();
			sizeIn = sizeIn + t;
            if (t < input.length() && code < L) {
				// Add s to symbol table
                st.put(input.substring(0, t + 1), code++);
				// add the size of the codeword because code was added
				sizeOut = sizeOut + W;
			}
			if(t < input.length() && code == L && W < 16) {
				// the current codebook is full and not at max bit length
				
				W = W + 1; // add one extra bit to codeword length
				L = L*2; // expand the maximum amount of codewords
				
				st.put(input.substring(0, t + 1), code++);
				sizeOut = sizeOut + W;
				// keep making codewords now
			}
			else if( code == L && W == 16) {
				// codebook is full, bit length is already 16, if reset mode then delete codebook reset stats
				if(mode == 1) {
					W = 9;
					L = 512;
					code = 0;
					st = new TST<Integer>();
					for (int i = 0; i < R; i++)
						st.put("" + (char) i, i);
					code = R+1;
					// will set bit length back to 9, max to 512, will create a new codebook and fill with single letter codewords
				}
				// else it will assume 0 and do nothing
			}
            input = input.substring(t);
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 

    public static void expand() {
		// read in the 2 bits that contain which mode the data was compressed in
		int mode = BinaryStdIn.readInt(2);
		
		// set so large because of max possible codebook size, if have time will make a resize function and make generic with L
        String[] st = new String[65537];
		
        int i; // next available codeword value
		boolean resetDoneFlag = false;
		
        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;
        String val = st[codeword];

        while (true) {
            BinaryStdOut.write(val);
			
			// dont read in more bits if codebook is full, reset then read
			if(i != L-1)
				codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);
            if (i < L) {
				//System.err.println("i= " + i + " L = " + L + " s =" + s + "  codeword= " + codeword);
				if( i == L-1 && W < 16) {
					// double the codebook and add to bit length
					{W=W+1;L=L*2;}
				}
				else if( i==L-1 && W == 16) {
				// codebook full of 16 bit codewords
					if(mode == 1) {
						// reset the codebook where the original program reset it
						W = 9;
						L = 512;
						st = new String[65537];
						for (i = 0; i < R; i++)
							st[i] = "" + (char) i;
						st[i++] = "";  
						codeword = BinaryStdIn.readInt(W);
						if (codeword == R) return;
						val = st[codeword];
						resetDoneFlag = true;
						// will set bit length back to 9, max to 512, will create a new codebook and fill with single letter codewords
					}
				}
				if( resetDoneFlag == false) {
					st[i++] = val + s.charAt(0);
				}
			}
			if(resetDoneFlag == false) {
				val = s;
			}
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
        if(args[0].equals("-"))
		{
			if(args[1].equals("n"))
				compress(0); // 0 means do nothing
			else if(args[1].equals("r"))
				compress(1); // 1 means reset
			else throw new IllegalArgumentException("Illegal command line argument");
		}
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}