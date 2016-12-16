<?php
session_start();
if(!isset($_SESSION['username']))
	header("Location: login.php");
if (!isset($_POST["fname"]) || !isset($_POST["lname"]) || !isset($_POST["email"]) || 
	!isset($_POST["subject"]) || !isset($_POST["descript"]))`// makes sure the form was filled out entirely
{
	header("Location: error.html");
}
$myconnection = new mysqli("localhost", "root", "", "Project2");
$sender_name = ($_POST["fname"]." ".$_POST["lname"]);
$sender_email = $_POST["email"];
$sender_subject = $_POST["subject"];	// sets up the ticket to insert into the table
$sender_descript = $_POST["descript"];
$query = "INSERT INTO tickets (sname, semail, ssubject,sdescript) 
VALUES ('$sender_name', '$sender_email', '$sender_subject','$sender_descript')"; 
if (mysqli_query($myconnection, $query)) 
{
	$query2 = "INSERT INTO ticket_status (current_status, admin_name) 
				VALUES ('open', 'Unassigned')"; 
	mysqli_query($myconnection, $query2);
	$reciever = $sender_email;					//setup and sends the emails to both the sender and the admins
	$subject = "Ticket Received!";
	$body = "Your ticket has been received successfully. Well fix your problem as soon as we can!";
	mail($reciever,$subject,$body);
	$query3 = "Select * FROM users WHERE admin_status='yes'";
	$result2 = mysqli_query($myconnection, $query2);
	$tempholder;
	$counter = 0;
	
	while($row = mysqli_fetch_assoc($result2)) 	// while there are still rows
	{
        $tempholder[$counter] = $row["email"];	// get the ticketid from the ticket_status table
		$counter++;									
    }
	foreach($tempholder as $value)
	{
		$reciever = $value;
		$subject = "Ticket Received!";
		$body = "A ticket has been submitted. Please investigate the issue!";
		mail($reciever,$subject,$body);
	}
   
} 
else 
	header("Location: error.html"); //incase something fails have them try again
$myconnection = null;
header("Location: confirmation.html");

?>