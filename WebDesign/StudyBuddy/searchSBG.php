<?php
session_start();
if(!isset($_SESSION['username'])){
	header('Location: homepage.php');
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
		#formregistration
		{
			font-family:Arial, Helvetica,sans-serif;
			text-wrap:normal;
			color:#759459;
		}
		
</style>
</head>
<body>
<center><img src="logo.png"></center>
<div id="formregistration">
<form name="form1" method="post" action="searchAllSBG.php">
<center><b><h2> <u>Search by Group Name or by Course of Study!</h2></u></b></br>
<label>Study Buddy Group Name:	<input type="text" name="sbgnamesearch" size="30" maxlength="30" /></label></br></br>
Or</br></br>
<label>Course of Study:	<input type="text" name="coursenamesearch" size="30" maxlength="30" /></label> </br></br> 
<input type="submit" name="submitButton" value="Search Now" style="color:white;background-color:#759459;border:1px solid black;padding:3px"/></br>
<font color="red" size="1"></br></center><center>*Please chose one or the other. NOT BOTH. Thank You.</center><font></br></br>
</form>
</div>
</body>
</html>