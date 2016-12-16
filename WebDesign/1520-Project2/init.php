

<?php
// SCRIPT IS DONE!!!!!!!!!! IT CREATES THE TABLES AND INSERTS THE STARTING DATA.
// the exact database schema can be found in the here.txt that i based the following code off of.
$myconnection = new mysqli("localhost", "root", "", "Project2");
$utflag = false;
$tkflag = false;
$tsflag = false;
///////////////////////////////////////////////////////Create the users table	
$query = "CREATE TABLE users(
	id INT(3) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(50) NOT NULL,
	password VARCHAR(50) NOT NULL,
	email VARCHAR(50),
	admin_status VARCHAR(3))";
if (mysqli_query($myconnection, $query)) 
{
    echo "The table users was successfully created\n";
	$utflag = true;
}
else
{
    echo "Error table users wasnt created\n";
}
////////////////////////////////////////////////////////Create the tickets table
$query = "CREATE TABLE tickets(
	id INT(3) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	sname VARCHAR(50) NOT NULL,
	semail VARCHAR(50) NOT NULL,
	ssubject VARCHAR(50) NOT NULL,
	sdescript VARCHAR(165) NOT NULL,
	date TIMESTAMP
	)";
if (mysqli_query($myconnection, $query)) 
{
    echo "The table tickets was successfully created\n";
	$tkflag = true;
}
else
    echo "Error table tickets wasnt created\n";
//////////////////////////////////////////////////////////////Creates the ticket_status table
unset($query);
$query = "CREATE TABLE ticket_status(
	id INT(3) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	current_status VARCHAR(6) NOT NULL,
	admin_name VARCHAR(50) NOT NULL	)";
if (mysqli_query($myconnection, $query))
{
    echo "The table ticket_status was successfully created\n";
	$tsflag = true;
}
else
    echo "Error table ticket_status wasnt created\n";

if ($utflag === true && $tkflag === true && $tsflag === true)
{
	$tempholder;
	// open flat files for admins
	$fp = fopen("admins.txt","r");
	$counter = 0;
	while (!feof($fp))
	{
		$tempholder[$counter] = fgets($fp);
		$counter++;
	}
	$fp = null;
	foreach($tempholder as $value)
	{
		if(strlen($value) > 1)
		{
			$temp2 = explode("#",$value);
			echo "\n";
			$username = $temp2[0];
			$password = $temp2[1];
			$email = $temp2[2];
			$email_length = (strlen($email) - 2); // used to chop off /r/n from fgets
			$aemail = substr($email,0,$email_length);
			$query = "INSERT INTO users (username, password, email, admin_status)
					  VALUES ('$username', '$password', '$email','yes')";
			if (mysqli_query($myconnection, $query))
				echo "Admin account successfully added.\n";
			else
				echo "Admin account was not successfully added!\n";
		}
	}
	// adds valid users to the table
	$counter = 0;
	unset($tempholder);
	unset($fp);
	$fp = fopen("users.txt","r");
	$counter = 0;
	while (!feof($fp))
	{
		$tempholder[$counter] = fgets($fp);
		$counter++;
	}
	$fp = null;
	foreach($tempholder as $value)
	{
		if(strlen($value) > 1)
		{
			$temp2 = explode("#",$value);
			echo "\n";
			$username = $temp2[0];
			$password = $temp2[1];
			$email = $temp2[2];
			$email_length = (strlen($email) - 2); // used to chop off /r/n from fgets
			$aemail = substr($email,0,$email_length);
			$query = "INSERT INTO users (username, password, email, admin_status)
					  VALUES ('$username', '$password', '$email','no')";
			if (mysqli_query($myconnection, $query))
				echo "User account successfully added.\n";
			else
				echo "User account was not successfully added!\n";
		}
	}
	// open flat files for test info to start
	$counter = 0;
	unset($tempholder);
	unset($fp);
	$fp = fopen("testfillups.txt","r");
	while (!feof($fp))
	{
		$tempholder[$counter] = fgets($fp);
		$counter++;
	}
	$fp = null;
	foreach($tempholder as $value)
	{
		if(strlen($value) > 1)
		{
			unset($query);
			$temp2 = explode("#",$value);
			$sender_name = $temp2[0];
			$sender_email = $temp2[1];
			$sender_subject = $temp2[2];
			$sender_descript = $temp2[3];
			$query = "INSERT INTO tickets (sname, semail, ssubject,sdescript) 
				VALUES ('$sender_name', '$sender_email', '$sender_subject','$sender_descript')";
			if (mysqli_query($myconnection, $query))
				echo "Ticket info successfully added.\n";
			else
				echo "echo Error: $query ". mysqli_error($myconnection)."\n";
			$tech_name = $temp2[4];
			$cstatus = $temp2[5];
			$status_length = (strlen($cstatus)); // used to chop off /r/n from fgets
			$cur_status = substr($cstatus,0,$status_length);
			unset($query);
			$query = "INSERT INTO ticket_status (current_status, admin_name) 
				VALUES ('$cur_status', '$tech_name')";
			if (mysqli_query($myconnection, $query))
				echo "Ticket status successfully added.\n";
			else
				echo "Ticket status was not successfully added!\n";	
		}
	}
	// use explode
}
// CREATE FLAGS THEN CHECK IF ALL FLAGS ARE CORRECT LOOP THROUGH FILE TO LOAD TABLE + ADMINS
$myconnection = null;
echo "Connection terminated, initialization is now complete.\n";
echo "You can now run the ticket system if all tables were successfully created.";
echo "You can also find several test tickets already added if the tables were successful.";
?>