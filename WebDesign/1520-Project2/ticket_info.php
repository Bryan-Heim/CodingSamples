<?php
session_start();
if(!isset($_SESSION['username']))
	header("Location: management.php");
if(!isset($_SESSION['idpicked']))
	header("Location: notpicked.html");
else
	$ticketid = $_SESSION['idpicked'];
?>
<html>
<body bgcolor=#d0ffd6>
<center>


<?php
$myconnection = new mysqli("localhost", "root", "", "Project2");
$query = "SELECT * FROM tickets WHERE id='$ticketid'";
		$result = mysqli_query($myconnection, $query);
		$row = mysqli_fetch_assoc($result);
		$id = $row['id'];
		$date = $row['date'];
		$sname = $row['sname'];
		$email = $row['semail'];
		$subject = $row['ssubject'];
		$sdescript = $row['sdescript'];		// simply gets the ticket form the id and displays all the info
		echo "<b>Ticket Id</b>: $id<br>			
		<b>Date Submitted</b>: $date<br>
		<b>Sender Name</b>: $sname<br>
		<b>Sender Email</b>: $email<br>
		<b>Subject</b>: $subject<br>
		<b>Description of Problem</b><br>
		$sdescript<br>";
		$query2 = "SELECT * FROM ticket_status WHERE id='$ticketid'";
		$result2 = mysqli_query($myconnection, $query2);
		$row2 = mysqli_fetch_assoc($result2);
		$admin_cur = $row2['admin_name'];
		$cur_stat = $row2['current_status'];
		echo "<b>Current Admin</b>: $admin_cur<br>
		<b>Status</b>: $cur_stat<br>";
?>
<br>
<h4>You can do the following options:</h4>
<br>
<form action="adminchoice.php" method="POST">
Open/Close the ticket
<input type="radio" name="choice" value="ooc">
<br>
Assign self to ticket
<input type="radio" name="choice" value="aft">
<br>
Remove self from ticket
<input type="radio" name="choice" value="rft">
<br>
Email the sender
<input type="radio" name="choice" value="ems">
<br>
Delete the ticket
<input type="radio" name="choice" value="del">
<br>
More sender's tickets
<input type="radio" name="choice" value="mst">
<br>
Find all similar tickets
<input type="radio" name="choice" value="sim">
<br>
Go back to main page
<input type="radio" name="choice" value="bck">
<br>
<input type="submit" name="submit" value="Sumbit Option">
<br>
</form>

</center>
</body>
</html>