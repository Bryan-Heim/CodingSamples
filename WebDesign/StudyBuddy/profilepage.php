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
		li a
		{
			text-decoration: none; 
			padding: .8em .75em; 
			background-color:#19873E;
		}
		li a:hover,a:active
		{
			background-color:#7A991A;
		}
		#logo
		{
			column-width:300px;
			-moz-column-width:300px;
			margin-left:10em;
			margin-top: 1em;
		}
		#whatNext
		{
			font-family:Arial, Helvetica,sans-serif;
			text-align:center;
			text-wrap:normal;
			color:#759459;
			width:24em;
			padding:0;
			margin-left:10em;
		}
		#topppLeft {
			position: absolute;
			margin-top: .25em;
			margin-left:1em;
		}
		#buddyOptions
		{
			font-family:Arial, Helvetica,sans-serif;
			font-size:24;
			text-wrap:normal;
			color:#759459;
			width:32em;
			padding:0;
			margin-left:41em;
			margin-top:-12em;
		}
</style>
</head>
<body>
<body>
<div id="topppLeft"><font face="Arial, Helvetica, sans-serif" color="white" size="10">
<?php
	$welcomeMsg = "Welcome ".$_SESSION['username']."!";
	echo "$welcomeMsg";
?>
</font></div>
<ul>
<li><a href="logout.php"><b><font color="white" face="arial">Logout</font></a></li>
</ul>
<div id="logo"> 
<center><img src="logo.png"></center>
</div>
<div id="whatNext">
From here you will be able to either create your own study group or search the site for pre-exisiting study groups.
</div>
<div id="buddyOptions">
<center>
<font size="6" >Create a New Study Buddy Group	<a href="createSBG.php">Here</a></font></br></br>
<font size="6" >Search for Study Buddy Groups	<a href="searchSBG.php">Here</a></font></br>
</center>
</div>