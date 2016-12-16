<?php # Script 2.0 - checklogin.php
session_start();
if ($_SERVER['REQUEST_METHOD'] == 'POST')
{
	require ('mysqli_connect.php');
	$enteredUsername = ($_POST['username']);
	$enteredPass = ($_POST['password']);
	if (empty($enteredUsername) || empty($enteredPass)) {
		echo 'Sorry you forgot to enter either a username or password!';
	} 
	else {
		$SQLCommand = "SELECT * FROM UserLogin WHERE UserName='$enteredUsername' and Password='$enteredPass'";
		$qRun = mysqli_query($dbc, $SQLCommand);
		if(mysqli_num_rows($qRun)==1){
			$_SESSION['username']= "$enteredUsername";
			$_SESSION['password']= "$enteredPassword";
			header('Location: successfulLogin.php');
				 
		} else {
			header('Location: loginFailed.html');
		}
	}
}
	 	
mysqli_close($dbc);

?>