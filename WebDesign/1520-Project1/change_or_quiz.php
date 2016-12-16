<?php
session_start();
if (!(isset($_SESSION['username'])))
	header('Location: home.php');	// once again, no session. not logged in, go home
if (isset($_POST['choice']))
{
	if($_POST['choice'] == 'change')	// they want to log out
	{
		setcookie('username', 0, time() + 3600); // sets to an hour ago so cookie is destroyed
		session_destroy();						// also cookie set to 0 to indicate false for secondary check in case something weird happens
		header('Location: home.php');			// terminate session and send back home
		die();
	}
	else
	{
		header('Location: setup_quiz.php');	// they want to take quiz
		die();
	}
}
else
{
	header('Location: index.php');	// they didnt pick one and just clicked submit, try again lol
	die();
}

?>