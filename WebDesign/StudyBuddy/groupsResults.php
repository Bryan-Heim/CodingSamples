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
		#Fail
		{
			font-family:Arial, Helvetica,sans-serif;
			text-wrap:normal;
			color:#759459;
		}
		
</style>
</head>
<body>
<center><img src="logo.png"></center>
<div id="Fail">
<center><h2>The Search Results Found these Groups in your Course of Subject:</h2></center>
<center>
<?php
	foreach($_SESSION['results'] as $key=>$value)
    	{
		echo '-  '.$value;
		echo "</br>";
    	}
?>
</center>
</div>
</body>
</html>