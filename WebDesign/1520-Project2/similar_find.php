<?php
session_start();
if (!isset($_SESSION['username']))
	header("Location: login.php");
$ticketid = $_SESSION['idpicked'];
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
$query = "SELECT * FROM tickets WHERE id='$ticketid'";
$result = mysqli_query($myconnection, $query);
$row = mysqli_fetch_assoc($result);
$tickets_subject = $row['ssubject'];
$title_words = explode(" ",$tickets_subject);
$counter = 0;
foreach($title_words as $value)
{
	if($value != 'a' || $value != 'A' || $value != 'the' || $value != 'The' || $value != 'with'
		|| $value != 'With') // this checks to get rid of a few common words that bear no meaning in the similiar search
	{
		$tempholder[$counter] = $value;
	}
	$counter++;
}
// tempholder now hold the words that need to be matched
//print_r($tempholder);
$counter = 0;
$tempholder2;
$query2 = "SELECT * FROM tickets";
$result2 = mysqli_query($myconnection, $query2);
while($row2 = mysqli_fetch_assoc($result2)) 	// while there are still rows
	{
        $tempholder2[$counter] = $row2["ssubject"];	// get the ticketid from the ticket_status table
		$counter++;									
    }
// $tempholder contains the words to be matched, $tempholder2 contains all of the subjects of all tickets subjects
//print_r($tempholder2);
$id_counter = 0;
$all_matched_ids;
$match_counter=0;
foreach($tempholder2 as $value) // for each of the subjects from every ticket
{
	$exp_sub_tik = explode(" ",$value); // takes each word of the ticket
	foreach($exp_sub_tik as $value3)	// for each word in the "to-be-checked"	subject
	{
		foreach($tempholder as $value2) // now all the words in the original subject will be checked one by one against all the words in the other subject
		{
			// used for test echo "VALUES: $value3 and VALUE: $value2<br>";
			if (strcmp($value3,$value2) == 0) // if the two words match
			{
				$match_counter++;
				if($match_counter > 1)	// if there were two or matched then add the id to the similar matched array
				{
					$query3 = "SELECT * FROM tickets WHERE ssubject='$value'";
					$result3 = mysqli_query($myconnection, $query3);
					$row3 = mysqli_fetch_assoc($result3);
					$tickid = $row3['id'];
					$all_matched_ids[$id_counter] = $tickid;
					$id_counter++;
				}
				// used for test echo "MatchCOUNTER: $match_counter<br>";
			}
		}
		
	}
	$match_counter = 0;
}
$all_matched = array_unique($all_matched_ids); // makes sure that there are no duplicate ids 
foreach($all_matched as $value)	// taken from the show_*_tickets php scripts to generate the buttons given all the values
{
	$query = "SELECT * FROM tickets WHERE id='$value'";
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