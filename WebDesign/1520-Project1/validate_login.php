<?php

session_start();

if (isset($_POST['username_prov']))		// makes sure username was filled out
{
	$attempted_username = $_POST['username_prov'];
}
if (isset($_POST['password_prov']))		// makes sure pword was filled out
{
	$attempted_password = $_POST['password_prov'];
}
if($_POST['username_prov']=="" || $_POST['password_prov']=="")
{
	header('Location: home.php');	// if they forgot something refresh home.php
	die();
}


$fp = fopen("passwords.txt", "r+");
$string_holder;
$counter = 0;
while (!feof($fp))
{
	$string_holder[$counter] = fgets($fp);	// collects all the username#password pairs in an array
	$counter++;
}
foreach ( $string_holder as $value)	// go through all username password combos
{
	$tempholder = explode("#",$value);	//separate strings
	$username = $tempholder[0];
	$password = $tempholder[1];
	if ( $attempted_username == $username )	// if the new name they tried matched old one
	{
		$length = strlen($attempted_password);		// get length to avoid end of line issues
		if (substr_compare($password,$attempted_password,0,$length)==0)	// check passwords
		{
			$_SESSION['username'] = $attempted_username;	// if equal setup session
			$_SESSION['password'] = $attempted_password;
			if (isset($_POST['keep_login']))
			{
				setcookie('username', "1", time() + (86400 * 30));	// if wanted, cookie sent for use on home.php
			}
			header('Location: index.php');	// successful session started, show them their choices
			die();
		}
	}
}
header('Location: login_failed.php');	// username was not found or password was wrong will redirect back home.php


?>