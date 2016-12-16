<?php # Script 1.0 - registration.php 

if ($_SERVER['REQUEST_METHOD'] == 'POST')
{
	require ('mysqli_connect.php');
	$tempUserName = ($_POST['username']);
	$password = null;
	if (empty($_POST['username'])) {
	 	header('Location: registrationerror.html');
 	} else{
		$username = $_POST['username'];
		$SQLCommand = "SELECT * FROM UserLogin WHERE UserName='$username'";
		$qRun = mysqli_query($dbc,$SQLCommand);
		if(mysqli_num_rows($qRun)==1){
			header('Location: registrationerrorunt.html');
		} else{
			$username = mysqli_real_escape_string($dbc, trim($_POST['username']));
			if (empty($_POST['password']) || empty($_POST['repassword']) ){
				header('Location: registrationerror.html');
			} else if (($_POST['password']) != ($_POST['repassword'])){
				header('Location: registrationerrorpdm.html');	
			} else{
				$password = mysqli_real_escape_string($dbc, trim($_POST['password']));
			}	
		}
	}
	
	if($password != Null)
	{
		$createQuery = "INSERT INTO UserLogin (UserName, Password) VALUES ('$username','$password')";
		$runQ = @mysqli_query($dbc, $createQuery);
		if($runQ){header('Location: registrationsuccess.html');	;}
		else{echo "Sorry there was an error in registering, please email the help center at StuddyBuddyHelp@studdybuddy.com!";}
	}

mysqli_close($dbc);
}

?>