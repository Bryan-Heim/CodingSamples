<?php
session_start();
if (!isset($_SESSION['username']))
	header("Location: management.php");
?>
<html>
<body bgcolor=#d0ffd6>
<center>
<br><br><br>
<h3><b>Please select how you would like to sort your table.</b></h3>
<form action="selectedchoice.php" method="post">
<br>
Sort by Ticketid
<input type='radio' name='sortby' value='ticketid' >
<br>
Sort by Date
<input type='radio' name='sortby' value='date' >
<br>
Sort by Sender Name
<input type='radio' name='sortby' value='sname' >
<br>
Sort by Email
<input type='radio' name='sortby' value='email' >
<br>
Sort by Subject
<input type='radio' name='sortby' value='subject' >
<br><br><br>
<b>Now pick where you would like to go:</b>
<br>
<input type="submit" name="val" value="View All Tickets">
</br>
<input type="submit" name="open" value="View Open Tickets">
</br>
<input type="submit" name="vst" value="View Selected Ticket">
</br>
<input type="submit" name="vmt" value="View My Tickets">
</br>
<input type="submit" name="vut" value="View Unassigned Tickets">
</form>



</center>
</body>
</html>