/*
	An implementation of RSA that uses BigInteger to generate p,q,n,phi(n),e and d
	Note: I left all the test strings in to verify each value along the way
	By Bryan Heim
*/

import java.util.*;
import java.io.*;
import java.math.BigInteger;
public class RSAKeyGen{

	public static void main(String[] args){
	
		Random rnd = new Random();
		BigInteger oneConst = new BigInteger("1");
		// setup the random to pass to BigInt constructor
		// setup the oneConst BigInt object for comparisons
		
		
		BigInteger temp = new BigInteger(1024, rnd);
		rnd = new Random();
		BigInteger p = temp.probablePrime(1024, rnd);
		//System.out.println("\nP: " + p.toString() + "\n");
		rnd = new Random();
		BigInteger q = temp.probablePrime(1024, rnd);
		//System.out.println("Q: " + q.toString() + "\n");
		// p and q are now generated and probably are prime.
		
		
		rnd = new Random();
		BigInteger n = new BigInteger(2048, rnd);
		n = q.multiply(p);
		//System.out.println("N: " + n.toString() + "\n");
		// get a value for n by p*q
		
		
		rnd = new Random();
		BigInteger phiN = new BigInteger(2048, rnd);
		BigInteger phiP = p.subtract(oneConst);
		BigInteger phiQ = q.subtract(oneConst);
		phiN = phiP.multiply(phiQ);
		//System.out.println("Phi(N): " + phiN.toString() + "\n");
		// get the values for phi(N) by phiP*phiQ or (P-1)*(Q-1)
		
		
		rnd = new Random();
		BigInteger e = new BigInteger(2048, rnd);
		// compareTo == 1 means greater, == 0 means equal, == -1 means less than
		// while e doesnt equal  e > 1, e < phiN and e.gcd(phiN) == 1, else good e found
		while(!((e.compareTo(oneConst) == 1) && (e.compareTo(phiN) == -1) && (e.gcd(phiN).compareTo(oneConst) == 0))) 
		{
			e = new BigInteger(2048, rnd);
		}
		//System.out.println("E: " + e.toString() + "\n");
		
		
		// found a valid e, calculate d using d = e^(-1)mod(phiN)
		BigInteger d = e.modInverse(phiN);
		//System.out.println("D: " + d.toString() + "\n");
		// at this point p,q,n,phiN,e and d are all generated
		// write them out to file
		
		//pubkey.rsa contain E and N
		//privkey.rsa contain D and N
		// saves values as string in file
		// will allow BigInt to be made from string val
		try{
			PrintWriter pub = new PrintWriter("pubkey.rsa");
			pub.print(e.toString() + "\n");
			pub.print(n.toString());
			pub.close();
			PrintWriter priv = new PrintWriter("privkey.rsa");
			priv.print(d.toString() + "\n");
			priv.print(n.toString());
			priv.close();
		}catch(Exception err){ System.out.println(err.getMessage()); }
		// let user know keys were made
		System.out.println("Keys successfully generated. Files 'privkey.rsa' and 'pubkey.rsa' created.");
	}
}
