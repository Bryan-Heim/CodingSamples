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
<form name="form1" method="post" action="checkNewSBG.php">
<center><b><h2> <u>Create Your Own Study Group!</h2></u></b></br>
<label>Study Buddy Group Name:	<input type="text" name="sbgname" size="30" maxlength="30" /></label></br></br>
<label>Course of Study:	<input type="text" name="coursename" size="30" maxlength="30" /></label> </br></br> 
<input type="submit" name="submitButton" value="Create Now!" style="color:white;background-color:#759459;border:1px solid black;padding:3px"/></br>
<font color="red" size="1"></br></center><center>*Both Fields ARE Required.</center><font></br></br>

</form>
</div>
</body>
</html>