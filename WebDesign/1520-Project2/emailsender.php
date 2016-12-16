<?php
session_start();
if (!isset($_SESSION['username']))
	header("Location: management.php");
if(!isset($_POST['subject']) || !isset($_POST['body']))
	header("Location: emailsender.php"); // make sure they filled out both parts, else go back
$subject = $_POST['subject'];
$body = $_POST['body'];
$username = $_SESSION['username'];
$ticketid = $_SESSION['idpicked'];	// set up the names and ids aswell as what to send
$myconnection = new mysqli("localhost", "root", "", "Project2");
$query = "SELECT semail FROM tickets WHERE id='$ticketid'";
$result = mysqli_query($myconnection, $query);	// find the ticket senders email and send email
$row = mysqli_fetch_assoc($result);
$emailto = $row['semail'];
$reciever = $emailto;
$subject = "Ticket Received!";
$body = "Your ticket has been received successfully. Well fix your problem as soon as we can!";
mail($reciever,$subject,$body);
header("Location: ticket_info.php");
?>

<html>
<body bgcolor=#d0ffd6>
<center>
<br>
<br>
<br>
<form action = "sendmail.php" method = "POST">
<b><h4>Subject:</h4></b>
<input type = "text" name = "subject" >
<br><br>
<b><h4>Body Of Email:</h4></b>
<textarea name="body" rows="6" cols="60"></textarea>
<br><br>
<input type = "submit" value = "Email Now">
</form>
</center>
</body>
</html>
