# Compilation Instructions  
##1653 - BPH11 - RAD114  

IMPORTANT NOTE: in order to use 256-bit AES keys, the current US_export_policy.jar, and local_policy.jar
must be replaced in your ${java.home}/lib/security/ folder with the US_export_policy.jar, 
and local_policy.jar files that you can get from the link below.  If this is not done, you will receive an invalid
key error, and the program will crash.
These two jar files, as well as the Bouncy Castle jar file have been included in the /src folder. 

IMPORTANT NOTE 2: This application requires Java 1.8 in order to function properly. There will be compile warnings, the functionality is still working.

###To compile the client and server code:  
 - Change directory into this repository main directory
 - Enter the `src/` directory of the repository  
 - Run command "javac -cp .:/bcprov-jdk15on-155.jar *.java" without double quotations

There are no errors in the compilation. 