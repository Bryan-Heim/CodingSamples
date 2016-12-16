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
	header('Location: login.php');	// if they forgot something, back to login
	die();
}
$myconnection = new mysqli("localhost", "root", "", "Project2");
$query = "SELECT * FROM users WHERE username='$attempted_username' and password='$attempted_password'";
$rows = mysqli_num_rows(mysqli_query($myconnection, $query));
if ($rows === 1)	// if the username and password combo matched a row
{
	$_SESSION['username'] = $attempted_username;
	$query2 = "SELECT * FROM users WHERE username='$attempted_username'";
	$result2 = mysqli_query($myconnection, $query2);
	$row2 = mysqli_fetch_assoc($result2);
	$admin_status = $row2['admin_status'];	// check if they are admin or not and redirect appropriately
	if ($admin_status == 'yes')
		header("Location: show_open_tickets.php");
	else
		header("Location: index.php");
}
else
	header('Location: login_failed.html');	// username was not found or password was wrong will redirect back home.php
$myconnection = null;
die();



?>