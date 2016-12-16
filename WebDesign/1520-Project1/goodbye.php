<?php
session_start();
if (!(isset($_SESSION['username'])))	// no username assigned, not logged in
	header('Location: home.php');
?>
<html>
<title>Redirecting</title>
<meta http-equiv="refresh" 
content="20;URL=http://localhost/bph11_Project1/home.php">
</head>
<style>
body {font-family: Arial, Helvetica, sans-serif;}
</style>
<body bgcolor=#d0ffd6>
<center>

<br><br><br><br>

<h1> Thank you for playing. </h1>

<br>

<h1> We hope to see you again soon! </h1>

<br>

<?php
	session_destroy();	// destroy their session
	setcookie("username", "", time() - 3600); // sets to an hour ago, gets rid of auto-login	
	die(); // ends script
?>

</center>
</body>
</html>