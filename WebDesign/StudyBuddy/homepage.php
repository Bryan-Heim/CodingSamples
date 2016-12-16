<?php
session_start();
if (isset($_SESSION['username'])) {
	header('Location: profilepage.php');
}
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title> Study Buddy: Studying With No Boundries </title>
<style>
		body {background-image:url('bgcolour.jpeg');
		margin: 0; }
		ul
		{
			margin: 0; 
			padding: 2em; 
			border: 0;
			list-style-type: none; 
			background-color:#19873E;
		}
		li
		{
			display:inline;
			float:right;
		}
		a
		{
			text-decoration: none; 
			padding: .8em .75em; 
			background-color:#19873E;
		}
		a:hover,a:active
		{
			background-color:#7A991A;
		}
		#logo
		{
			column-width:300px;
			-moz-column-width:300px;
			margin-left:10em;
		}
		#intro
		{
			font-family:Arial, Helvetica,sans-serif;
			text-align:center;
			text-wrap:normal;
			color:#759459;
			width:24em;
			padding:0;
			margin-left:10em;
		}
		#formregistration
		{
			font-family:Arial, Helvetica,sans-serif;
			text-wrap:normal;
			color:#759459;
			width:32em;
			padding:0;
			margin-left:41em;
			margin-top:-20em;
		}
</style>
</head>
<body>
<ul>
<li><a href="#Contact"><b><font color="white" face="arial">Contact Us</font></a></li>
<li><a href="login.php"><font color="white" face="arial">Login</font></b></a></li>
</ul>
<div id="logo"> 
<center><img src="logo.png"></center>
</div>
<div id="intro">
<h2>Welcome to Studdy Buddy!</h2>
<b>Our goal is to break down the barriers
 that divide students from getting the
 help they need. Here at Studdy Buddy 
 you will be able to join live study 
 sessions in the subject of your chosing.
 To access the site simply register on this page.</b>
</div>
<div id="formregistration">
<form action="registration.php" method="post">
<center><b><h2> <u>New User? Register Here </h2></u></b></br>
<label>Desired Username:	<input type="text" name="username" size="30" maxlength="30" /></label></br></br>
<label>Enter Password:	<input type="password" name="password" size="30" maxlength="30" /></label> </br></br> 
<label>Re-Enter Password:	<input type="password" name="repassword" size="30" maxlength="30" /></label></br></br>
<input type="submit" name="submitButton" value="Register Now!" style="color:white;background-color:#759459;border:1px solid black;padding:3px"/>
<font color="red" size="1"></center><center>*All Fields Required</center><font></br></br>
</form>
</div>
</body>
</html>