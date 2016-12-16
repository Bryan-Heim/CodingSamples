<?php
/////////////////////////////////////////// THE MAIN HOMEPAGE FOR THE DAILY QUIZ ///////////
if (isset($_COOKIE['username']))
{
	if(($_COOKIE['username'])==1) {	// checks too see if cookie is set and if its 1 (keep me login in selected)
		session_start();			// if it is 1, then start the users session without login required.
		header('Location: index.php');
		die();
	}
}
?>
<style>
body {font-family: Arial, Helvetica, sans-serif;}
</style>
<html>
<body bgcolor=#d0ffd6>
<center>

<br><br><br><br>

<h1> Welcome To Quiz Of The Day! </h1>
<h2> A websited designed to give you your daily dose of trivia </h2>
<br>
<h3>
<h4> Already a member? Login Now! </h4>
<form action="validate_login.php" method="post">
Username:
<input type="text" name="username_prov" value=""> <br>
Password:
<input type="text" name="password_prov" value="">
</h3>
<br>
<input type="checkbox" name="keep_login" value="yes"> Keep Me Logged In
<br><br>

<input type="submit" value="Login!">
</form>
<br>
<h4>
<a href="register.html"> Not a member? Click Here to Join Now.</a>
</h4>

</center>
</body>
</html>