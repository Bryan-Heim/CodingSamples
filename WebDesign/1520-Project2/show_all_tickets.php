<?php
session_start();
if (!isset($_SESSION['username']))
	header("Location: management.php");
$username = $_SESSION['username'];
if (isset($_SESSION['sortby']))	// used for the sortby function
	$sortby = $_SESSION['sortby'];
else	
	$sortby = 'id';

?>

<html>
<body bgcolor=#d0ffd6>
<center>
<pre>
<b>
Ticket Num	Received	Sender Name	Sender Email	Subject
	Admin Assigned	Status
</b>
<hr>
</pre>
<form action="selectedchoice.php" method="post">
<?php

$myconnection = new mysqli("localhost", "root", "", "Project2");
$query = "SELECT * FROM ticket_status";	// gets all the tickets 
$result = mysqli_query($myconnection, $query);
$rowtotal = mysqli_num_rows($result);
$counter = 0;
$tempholder;
if ($rowtotal != 0) 
{
    while($row = mysqli_fetch_assoc($result)) 	// while there are still rows
	{
        $tempholder[$counter] = $row["id"];	// get the ticketid from the ticket_status table
		$counter++;									
    }
	//tempholder now holds all the ticketids that had a status of open
	foreach($tempholder as $value)
	{
		$query = "SELECT * FROM tickets WHERE id='$value' ORDER BY $sortby ASC";
		$result = mysqli_query($myconnection, $query);
		while($row = mysqli_fetch_assoc($result))
		{
			$id = $row['id'];
			$date = $row['date'];
			$sname = $row['sname'];
			$email = $row['semail'];
			$subject = $row['ssubject'];
			echo "<pre>$id\t$date\t$sname\t$email\t$subject\t</pre>";
			$query2 = "SELECT * FROM ticket_status WHERE id='$id'";
			$result2 = mysqli_query($myconnection, $query2);
			$row2 = mysqli_fetch_assoc($result2);
			$admin_name = $row2['admin_name'];
			$current_stat = $row2['current_status'];
			echo "<pre>$admin_name\t$current_stat\t</pre>";
			echo "<input type='radio' name='ticketpicked' value='$id' >";
			echo "<hr>";
		}
		//print loop, get the ticket id, find the ticket in the tickets table, print the ticket
	}
	//print options for view all tables, sort, logout, etc
	//
} 
else
{
    echo "Currently, there are no tickets opened!";
}
?>
</br>
<input type="submit" name="open" value="View Open Tickets">
<input type="submit" name="sort" value="Sort">
<input type="submit" name="vst" value="View Selected Ticket">
</br>
<input type="submit" name="vmt" value="View My Tickets">
<input type="submit" name="logo" value="Logout">
<input type="submit" name="vut" value="View Unassigned Tickets">
</form>

</center>
</body>
</html>