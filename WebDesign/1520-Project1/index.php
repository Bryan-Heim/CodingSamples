<?php
session_start();
if (isset($_SESSION['username']))
	$username = $_SESSION['username'];	//sets up a local variable for use  later
else
	header('Location: home.php');	// no session, not logged in, send back home
?>


<html>
<style>
body {font-family: Arial, Helvetica, sans-serif;}
</style>
<body bgcolor=#d0ffd6>
<center>

<br><br><br><br>
<?php
	echo "<h1> Welcome $username</b> </h1>"; // show their username!
?>
<br>
<h3> Please Chose What You Like To Do:  </h3>
<br>

<form action="change_or_quiz.php" method="post">
<input type="radio" name="choice" value="quiz"> Take Daily Quiz!
<br>
<input type="radio" name="choice" value="change"> Change User!
</h3>
<br><br>

<input type="submit" value="Submit">
</form>



</center>
</body>
</html>