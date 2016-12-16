/*
Name: Bryan Heim
Class: CS1501 TuTh 1-2:15PM/Fri 10-10:50AM
Project: Assignment 5 RSA Implementation
Details: First program checks to make sure
arguments were given and correctly formatted
and the given file exists. Then either s or v mode
will run but will throw an error if no key files
were found. S mode creates a sha-256 of the original
then generates the hash/key and makes .signed file.
v mode then loads in the .signed file and compares
the signatures made, afterwards it tells the users
if the signature is valid. Also test print out
statements were left in and commented for testing purposes.
*/

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.math.BigInteger;
import java.security.MessageDigest;
public class RSASignTest{

	public static void main(String[] args){
	
		if(args.length != 2)
			System.out.println("Sorry, command argument format incorrect/missing.");
		else
		{
			String mode = args[0];
			String fileName = args[1];
			File fileWanted = new File(fileName);
			if(mode.equals("s"))
			{
				// check if privkey.rsa exists if not error exit
				File priv = new File("privkey.rsa");
				if(priv.exists())
				{
					if(fileWanted.exists())
					{
						// generate the sha-256 hash
						// I will lazily catch any exception as-well
						try{
						
						
							Path path = Paths.get(fileName);
							byte[] data = Files.readAllBytes(path);
							MessageDigest md = MessageDigest.getInstance("SHA-256");
							md.update(data);
							byte[] digest = md.digest();
							BigInteger hash = new BigInteger(1, digest);
							//System.out.println("Original: " + hash.toString());
							
							// message is hashed and prepared
							// get the values for d and n from privkey.rsa
							BufferedReader privK = new BufferedReader(new FileReader("privkey.rsa"));
							String dStr = privK.readLine();
							String nStr = privK.readLine();
							BigInteger d = new BigInteger(dStr);
							BigInteger n = new BigInteger(nStr);
							BigInteger finalHash = hash.modPow(d,n);
							
							
							// all calculations/hashes done at this point
							// create a new filname.signed file with original file contents and hash
							// print out all of the bytes of the hash and then the original data
							FileOutputStream fos = new FileOutputStream(fileName + ".signed");
							ObjectOutputStream oos = new ObjectOutputStream(fos);
							oos.write(finalHash.toByteArray());
							oos.write(data);
							oos.close();
							System.out.println("The file '" + fileName + "' has been successfully signed.");
							System.exit(0);
						}catch(Exception err){ System.out.println(err.getMessage()); }
					}
					else
						System.out.println("Sorry, no file with name '" + fileName + "' was found.");
				}
				else
					System.out.println("Sorry, no privkey.rsa file was found in current directory.");
			}
			else if(mode.equals("v"))
			{
				// check if pubkey.rsa exists, if not error exit
				File pub = new File("pubkey.rsa");
				if(pub.exists())
				{
					if(fileWanted.exists())
					{
						try{
							FileInputStream fis = new FileInputStream(fileName);
							ObjectInputStream ois = new ObjectInputStream(fis);
							int totalBytes = ois.available();
							byte[] hashFromFile = new byte[256]; // 2048 bit signature = 256 bytes
							byte[] data = new byte[totalBytes-256]; // the total number of bits from original file, exluding signature size
							for(int i = 0; i < totalBytes; i++) // load the signature and data in
							{
								if(i < 256)
									hashFromFile[i] = ois.readByte();
								else
									data[i-256] = ois.readByte();
							}
							ois.close();
							
							MessageDigest md = MessageDigest.getInstance("SHA-256");
							md.update(data);
							byte[] digest = md.digest();
							BigInteger hashMade = new BigInteger(1, digest);
							// recompute the sha for the original data
							
							BigInteger hashGiven = new BigInteger(1, hashFromFile);
							BufferedReader pubK = new BufferedReader(new FileReader("pubkey.rsa"));
							String eStr = pubK.readLine();
							String nStr = pubK.readLine();
							BigInteger e = new BigInteger(eStr);
							BigInteger n = new BigInteger(nStr);
							BigInteger hashEncrypted = hashGiven.modPow(e,n);
							// use the signature given to check against the signature made
							// if they are the same, it is valid!
							if(hashEncrypted.compareTo(hashMade) == 0)
								System.out.println("Signature is valid!");
							else
								System.out.println("Signature is invalid!");
								
							//System.out.println("hashE: " + hashEncrypted.toString() + "\n");
							//System.out.println("hashM: " + hashMade.toString() + "\n");
						}catch(Exception err){ System.out.println(err.toString()); }
					}
					else
						System.out.println("Sorry, no file with name '" + fileName + "' was found.");
				}
				else
					System.out.println("Sorry, no pubkey.rsa file was found in current directory.");
			}
			else
				System.out.println("Sorry, mode not correct. Use 's' or 'v'.");
		}
	
	}

}
