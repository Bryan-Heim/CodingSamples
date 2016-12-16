/*
	A test program that makes use of the BouncyCastle cryptography library
	Tests AES, BlowFish and RSA encryption/decryption as well as RSA verify/sign functionality
	By Bryan Heim
*/

import java.util.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

// import the bouncy castle provider
import org.bouncycastle.jce.provider.BouncyCastleProvider;
// to print the key to string
import org.bouncycastle.util.encoders.Hex;

public class CryptoTest {
	// set-up the cipher used by AES/Blowfish/RSA, starting iv's for AES/Blowfish, a secure random for RSA
	private static Cipher cipher = null; 
	private static String encrypted = "", decrypted = "";
	private static byte[] ivBytesAES = {(byte)0x07, (byte)0xb2, (byte)0x1d, (byte)0x86, (byte)0xc8, (byte)0x42, (byte)0x3f, (byte)0x51, (byte)0xf2, (byte)0xb2, (byte)0x03, (byte)0xa6, (byte)0x48, (byte)0x29, (byte)0x3b, (byte)0x56};
	private static byte[] ivBytesBlf = {(byte)0x07, (byte)0xb2, (byte)0x1d, (byte)0x86, (byte)0xc8, (byte)0x42, (byte)0x3f, (byte)0x51};
	private static SecureRandom random = new SecureRandom();
	
	// start the tests
	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		if(args.length > 0)	{
			// get the string from the command line and run the tests!
			String used = args[0];
			System.out.println("Welcome to the Crypto Test! Starting tests...");
			System.out.println();
			AESTest(used);
			BlowfishTest(used);
			RSATest(used);
			randoStringTester();
			System.out.println("All tests finished. Now exiting the test application. Goodbye!");
		}
		else
			System.out.println("Failed to start, string need as an argument.");
		return;
	}

	/**.
	 * Run AES encryption/decryption on the string given as argument
	 * @param test - the string to be tested
	 */
	private static void AESTest(String test) {
		Security.addProvider(new BouncyCastleProvider());
		
		// random ivBytes to start the encrypt process
		byte[] argIn = test.getBytes();
		System.out.println("Now beginning AES test for string \""+test+"\"");
		
		// generate the 128-bit key
		System.out.println("Generating 128-bit key...");
		Key theKey = generateCryptoKey("AES",true);
		
		// if key was successfully created
		if(theKey != null) {
			System.out.println("Encrypting the string \""+test+"\" using AES...");
			byte[] en = encryptString(test,"AES",theKey,true);
			if(en != null) {
				byte[] de = decryptString(en,"AES",theKey,true);
				if(de == null) {
					System.out.println("Decryption process failed. AES test failed.");
				}
			}
			else {
				System.out.println("Encryption process failed. AES test failed.");
			}
		}
		else {
			System.out.println("Key creation failed. AES test failed.");
		}
		
		// test are now all finished
		System.out.println("AES test has concluded");
		System.out.println();
		return;
	}

	/**.
	 * Run BlowFish encryption/decryption on the string given as argument
	 * @param test - the string to be tested
	 */
	private static void BlowfishTest(String test) {	
		Security.addProvider(new BouncyCastleProvider());
		// random ivBytes to start the encrypt process
		byte[] argIn = test.getBytes();
		System.out.println("Now beginning Blowfish test for string \""+test+"\"");
		
		// generate the 128-bit key
		System.out.println("Generating 128-bit key...");
		Key theKey = generateCryptoKey("Blowfish",true);
		
		// if key was successfully created
		if(theKey != null) {
			System.out.println("Encrypting the string \""+test+"\" using Blowfish...");
			byte[] en = encryptString(test,"Blowfish",theKey,true);
			if(en != null) {
				byte[] de = decryptString(en,"Blowfish",theKey,true);
				if(de == null) {
					System.out.println("Decryption process failed. Blowfish test failed.");
				}
			}
			else {
				System.out.println("Encryption process failed. Blowfish test failed.");
			}
		}
		else {
			System.out.println("Key creation failed. Blowfish test failed.");
		}
		
		// test are now all finished
		System.out.println("Blowfish test has concluded");
		System.out.println();
		return;
	}

	/**.
	 * Run RSA encryption/decryption and sign and verify on the test string
	 * @param test - the string to be tested
	 */
	private static void RSATest(String test) {
		Security.addProvider(new BouncyCastleProvider());
		System.out.println("Now beginning RSA test for string \""+test+"\"");
		
		// generate public and private keys
		System.out.println("Creating 2048-bit private/public key pair...");
		KeyPair keysRSA = generateCryptoKeyRSA(true);
		if(keysRSA != null)	{
			// get the keys by themselves
			Key pubK = keysRSA.getPublic();
			Key privK = keysRSA.getPrivate();
			
			// try to encrypt the test string
			System.out.println();
			System.out.println("Attempting to encrypt string \""+test+"\" using RSA...");
			byte[] en = encryptStringRSA(test,pubK,true);
			if(en != null) {
				// try to decrypt the result from the encryption
				System.out.println("Attempting to decrypt string result using RSA...");
				byte[] de = decryptStringRSA(en,privK,true);
				if(de != null) {
					// generate signature for the test string
					System.out.println("Creating signature for string \""+test+"\" using RSA...");
					if(!(signAndVerifyString(test,keysRSA,true))) {
						System.out.println("Signature process failed. RSA test failed.");
					}
				}
				else {
					System.out.println("Decryption process failed. RSA test failed.");
				}
			}
			else {
				System.out.println("Encryption process failed. RSA test failed.");
			}
		}
		else {
			System.out.println("Public and private key creation failed. RSA test failed.");
		}
		
		// RSA test has finished
		System.out.println("RSA test has concluded");
		System.out.println();
		return;
	}
	
	/**.
	 * generate 100 random strings, compare how fast AES/Blowfish/RSA take to encrypt them
	 */
	private static void randoStringTester() {
		Security.addProvider(new BouncyCastleProvider());
		long beginTiming = 0, endTiming = 0, aesTime = 1, blfTime = 1, rsaTime = 1, toSeconds = 1000000;
		double aesRSA = 1, blfRSA = 1, blfAES = 1;
		System.out.println("Now beginning benchmark tests");
		
		// generate the strings
		System.out.println("Creating 100 random strings for benchmark...");
		String[] randos = new String[100];
		for(int i = 0; i < 100; i++) {
			randos[i] = getRandomString(30); // of length 30
			//System.out.println("Random: " + randos[i]); to check random strings were random
		}
		System.out.println("Successfully generated 100 random strings each 30 characters long");
		
		// create AES, Blowfish, RSA public, and RSA private keys
		Key keyAES = generateCryptoKey("AES",false);
		Key keyBLF = generateCryptoKey("Blowfish",false);
		KeyPair keysRSA = generateCryptoKeyRSA(false);
		Key keyPublic = keysRSA.getPublic();
		Key keyPrivate = keysRSA.getPrivate();
		
		// time AES
		System.out.println("Timing how long AES takes to encrypt the strings...");
		beginTiming = System.nanoTime();
		for(int i = 0; i < 100; i++) {
			encryptString(randos[i],"AES",keyAES,false); // print off for better speed
		}
		endTiming = System.nanoTime();
		aesTime = (endTiming - beginTiming);
		System.out.println("Successfully recorded AES's time");
		
		// time Blowfish
		System.out.println("Timing how long Blowfish takes to encrypt the strings...");
		beginTiming = System.nanoTime();
		for(int i = 0; i < 100; i++) {
			encryptString(randos[i],"Blowfish",keyBLF,false); // print off for better speed
		}
		endTiming = System.nanoTime();
		blfTime = (endTiming - beginTiming);
		System.out.println("Successfully recorded Blowfish's time");
		
		// time RSA
		System.out.println("Timing how long RSA takes to encrypt the strings...");
		beginTiming = System.nanoTime();
		for(int i = 0; i < 100; i++) {
			encryptStringRSA(randos[i],keyPublic,false); // print off for better speed
		}
		endTiming = System.nanoTime();
		rsaTime = (endTiming - beginTiming);
		System.out.println("Successfully recorded RSA's time");
		
		// show the results
		System.out.println("The results are as follows");
		System.out.println("	- AES: " + (aesTime/toSeconds) + " milliseconds");
		System.out.println("	- Blowfish: " + (blfTime/toSeconds) + " milliseconds");
		System.out.println("	- RSA: " + (rsaTime/toSeconds) + " milliseconds");
		
		// calculate the differences and display the results
		aesRSA = (double)rsaTime/(double)aesTime;
		blfRSA = (double)rsaTime/(double)blfTime;
		blfAES = (double)blfTime/(double)aesTime;
		
		// printf is used for formatting the doubles
		System.out.println("From the results above we can establish that");
		System.out.printf("	- AES is about %.2f times faster than RSA",aesRSA);
		System.out.println();
		System.out.printf("	- Blowfish is about %.2f times faster than RSA",blfRSA);
		System.out.println();
		System.out.printf("	- AES is about %.2f times faster than Blowfish",blfAES);
		System.out.println();

		// random string benchmark test now finished
		System.out.println("Benchmark test has concluded");
		System.out.println();
		return;
	}
	
	/**.
	 * Creates a random string from the given alphabet
	 * @param length - the size of the random
	 * @return String - the randomized string
	 */
	private static String getRandomString(int length) {
		int randomIndex = -1;
		String random = "";
		StringBuilder daBuilda = new StringBuilder();
		// alphabet = all possible unique keys on standard keyboard
		String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()~`-+=_/\\?><,.:;\"'{}[]|";
		int alength = alphabet.length();
		for(int i = 0; i < alength; i++) {
			// pick random index between 0 and alphabet.length-1
			Random reallyRandom_Not = new Random();
			randomIndex = reallyRandom_Not.nextInt(alength);
			daBuilda.append(alphabet.charAt(randomIndex));
		}
		random = daBuilda.toString();
		return random;
	}
	
	/**.
	 * Uses Bouncy Castle to create a Key used for either AES or BlowFish
	 * @param mode - "AES" or "Blowfish" to determine which type of key to create
	 * @param print- used if you wish to print results to terminal
	 * @return Key - the symmetric crypto-key created
	 */
	private static Key generateCryptoKey(String mode, boolean print) {
		Key theKey;
		try {
			// make a key generate using bouncy castle and AES/Blowfish
			KeyGenerator theGenerator = KeyGenerator.getInstance(mode, "BC");
			// because we are using a 128 bit key
			theGenerator.init(128);
			// gen the actual key
			theKey = theGenerator.generateKey();
			
			if(print) {
				System.out.println("Successfully created 128 bit key");
				String keyString = new String(Hex.encode(theKey.getEncoded()));
				System.out.println("Result: " + keyString);
			}
		}catch(Exception e){theKey = null;}	
		return theKey;
	}
	
	/**.
	 * encrypt a string using either AES/Blowfish
	 * @param toEncryptStr - the string that will be encrypted
	 * @param mode - "AES" or "Blowfish" to determine which type of key to create
	 * @param theKey - the key that will be used for encryption
	 * @param print- used if you wish to print results to terminal
	 * @return byte[] - the results of the encrypted string stored as an array of bytes
	 */
	private static byte[] encryptString(String toEncryptStr, String mode, Key theKey, boolean print) {
		byte[] encryptedInput, ivBytes, toEncrypt = toEncryptStr.getBytes();
		
		// AES requires 16 byte iv block, blowfish requires 8
		if(mode.equals("AES")) {
			ivBytes = ivBytesAES;
		}
		else {
			ivBytes = ivBytesBlf;
		}
			
		// try encryption
		try	{
			// set-up the cipher using bouncy castle, AES or Blowfish, Cipher Block Chaining, and PKCS7 for padding
			cipher = Cipher.getInstance(mode+"/CBC/PKCS7Padding", "BC");
			// set-up needed state for encryption
			cipher.init(Cipher.ENCRYPT_MODE, theKey, new IvParameterSpec(ivBytes));
			// set-up space for the encrypted bytes to go
			encryptedInput = new byte[cipher.getOutputSize(toEncrypt.length)];
			// now encrypt
			int encryptedLength = cipher.update(toEncrypt, 0, toEncrypt.length, encryptedInput, 0);
			encryptedLength += cipher.doFinal(encryptedInput, encryptedLength);
			encrypted = new String(encryptedInput);
				
			if(print) {
				System.out.println("Encryption successful");
				System.out.println("Result: " + encrypted);
			}
		}catch(Exception e){encryptedInput = null; e.getMessage(); e.printStackTrace();}	
		return encryptedInput;
	}
	
	/**.
	 * decrypt the string that was encrypted by encryptString for AES/Blowfish
	 * @param toDecrypt - the encrypted byte array created by encryptString
	 * @param mode - "AES" or "Blowfish" to determine which type of key to create
	 * @param theKey - the key that will be used for encryption
	 * @param print- used if you wish to print results to terminal
	 * @return byte[] - the results of the decryption process
	 */
	private static byte[] decryptString(byte[] toDecrypt, String mode, Key theKey, boolean print) {
		byte[] decryptedInput, ivBytes;
		
		// AES requires 16 byte iv block, blowfish requires 8
		if(mode.equals("AES")) {
			ivBytes = ivBytesAES;
		}
		else {
			ivBytes = ivBytesBlf;
		}
			
		// try decryption
		try	{
			// now set-up the cipher to decrypt using the same key as it was encrypted with
			cipher.init(Cipher.DECRYPT_MODE, theKey, new IvParameterSpec(ivBytes));
			// set-up space for the decrypted bytes to go
			decryptedInput = new byte[cipher.getOutputSize(toDecrypt.length)];
			// now decrypt
			int decryptedLength = cipher.update(toDecrypt, 0, toDecrypt.length, decryptedInput, 0);
			decryptedLength += cipher.doFinal(decryptedInput, decryptedLength);
			decrypted = new String(decryptedInput);
			
			if(print) {
				System.out.println("Decryption successful");
				System.out.println("Result: " + decrypted);
			}
		}catch(Exception e){decryptedInput = null;}	
		return decryptedInput;
	}
	
	/**.
	 * generate a public/private key pair for RSA
	 * @param print- used if you wish to print results to terminal
	 * @return KeyPair - the object which contains both the RSA private and public keys
	 */
	private static KeyPair generateCryptoKeyRSA(boolean print) {
		KeyPair keysRSA;
		try{
			// init key generate for RSA using Bouncy Castle
			KeyPairGenerator theGenerator = KeyPairGenerator.getInstance("RSA", "BC");
			// Using 2048bit, init the key generator
            theGenerator.initialize(2048, new SecureRandom());
			// create the actual pair
			keysRSA = theGenerator.generateKeyPair();
			
			if(print) {
				// get the keys out and parse into strings
				Key pubK = keysRSA.getPublic();
				String pubKStr = new String(Hex.encode(pubK.getEncoded()));
				Key privK = keysRSA.getPrivate();
				String privKStr = new String(Hex.encode(privK.getEncoded()));
				
				// print the keys
				System.out.println("Successfully created public key:");
				System.out.println("Result: " + pubKStr);
				System.out.println();
				System.out.println("Successfully created private key:");
				System.out.println("Result: " + privKStr);
				System.out.println("Note: this key should always be kept absolutely secret!");
			}
		}catch(Exception e){keysRSA = null;}
		return keysRSA;
	}
	
	/**.
	 * Encrypt a string using the RSA public key
	 * @param toEncryptStr - the string that will be encrypted
	 * @param pubK - the RSA public key object to use for encryption
	 * @param print- used if you wish to print results to terminal
	 * @return byte[] - the bytes from the string after encrypting
	 */
	private static byte[] encryptStringRSA(String toEncryptStr, Key pubK, boolean print) {
		byte[] encryptedInput, toEncrypt = toEncryptStr.getBytes();
		try	{
			// set-up the cipher using bouncy castle, RSA, and PKCS7 for padding
			cipher = Cipher.getInstance("RSA", "BC");
			// set-up needed state for encryption
			cipher.init(Cipher.ENCRYPT_MODE, pubK, new SecureRandom());
			// set-up space for the encrypted bytes to go
			encryptedInput = new byte[cipher.getOutputSize(toEncrypt.length)];
			// now encrypt
			int encryptedLength = cipher.update(toEncrypt, 0, toEncrypt.length, encryptedInput, 0);
			encryptedLength += cipher.doFinal(encryptedInput, encryptedLength);
			encrypted = new String(encryptedInput);
			
			if(print) {
				System.out.println("Encryption successful");
				System.out.println("Result: " + encrypted);
			}
		}catch(Exception e){encryptedInput = null;}
		return encryptedInput;
	}
	
	/**.
	 * Take the byte array that was created from encryptStringRSA and decrypt it
	 * @param toDecrypt - the encrypted string bytes
	 * @param privK - the RSA private key object needed in order to decrypt
	 * @param print- used if you wish to print results to terminal
	 * @return byte[] - the decrypted string bytes
	 */
	private static byte[] decryptStringRSA(byte[] toDecrypt, Key privK, boolean print) {
		byte[] decryptedInput;
		try {
			// now set-up the cipher to decrypt using the same key as it was encrypted with
			cipher.init(Cipher.DECRYPT_MODE, privK, new SecureRandom());
			// set-up space for the decrypted bytes to go
			decryptedInput = new byte[cipher.getOutputSize(toDecrypt.length)];
			// set-up space for the decrypted bytes to go
			decryptedInput = new byte[cipher.getOutputSize(toDecrypt.length)];
			// now encrypt
			int decryptedLength = cipher.update(toDecrypt, 0, toDecrypt.length, decryptedInput, 0);
			decryptedLength += cipher.doFinal(decryptedInput, decryptedLength);
			decrypted = new String(decryptedInput);;
			
			if(print) {
				System.out.println("Decryption successful");
				System.out.println("Result: " + decrypted);
			}
		}catch(Exception e){decryptedInput = null;}
		return decryptedInput;
	}

	/**.
	 * Takes a test string, signs it, and then verifies the signature was correct
	 * @param test - the string used for testing
	 * @param rsaKeys - a KeyPair object that stores a public/private RSA key pair
	 * @param print- used if you wish to print results to terminal
	 * @return boolean - indicate whether or not the sign/verify process was successful or not
	 */
	private static boolean signAndVerifyString(String test, KeyPair rsaKeys, boolean print) {
		byte[] signedBytes, inputBytes = test.getBytes();
		try	{
			// set-up the signature to use a SHA256 hash and RSA
			Signature signature = Signature.getInstance("SHA256withRSA", "BC");
			// init the signature 
			signature.initSign(rsaKeys.getPrivate(), new SecureRandom());
			// update signature 
			signature.update(inputBytes);
			// now sign with the signature
			signedBytes = signature.sign();
			String signed = new String(signedBytes);
			
			if(print) {
				System.out.println("Signature successful");
				System.out.println("Result: " + signedBytes);
			}
			
			// now init to verify using the public key
			signature.initVerify(rsaKeys.getPublic());
			// we want to verify the that the signature comes from that private key on test string
			signature.update(inputBytes);
			// print the results from verifying
			if(print) {
				System.out.println("Verification successful");
				System.out.print("Result: ");
				System.out.println(signature.verify(signedBytes));
			}
		}catch(Exception e){return false;}
		return true;
	}
	
}