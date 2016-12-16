<?php
session_start();
?>

<html>
<body>

<?php
if (isset($_POST['username_new']))
{
	$attempted_username = $_POST['username_new'];
}
if (isset($_POST['password_new']))
{
	$attempted_password = $_POST['password_new'];
}
if($_POST['username_new']=="" || $_POST['password_new']=="")
{
	header('Location: register.html');
	die();
}
$fp = fopen("passwords.txt", "r+");
$string_holder;
$counter = 0;
$flag = true;
while (!feof($fp))
{
	$string_holder[$counter] = fgets($fp);
	$counter++;
}
foreach ( $string_holder as $value)
{
	$tempholder = explode('#',$value);
	if ( $attempted_username == $tempholder[0] )
	{
		$test_string = $tempholder[0];
		header('Location: taken_error.php');
		$flag = false;
		die();
	}
	unset($tempholder);
}
if ($flag != false)
{
	$tempholder = explode("#",$string_holder[0]);
	$_SESSION['test'] = $tempholder[0];
	$string_to_write = $attempted_username."#".$attempted_password."\r\n";
	$store_word = $string_to_write.file_get_contents("passwords.txt");
	file_put_contents("passwords.txt", $store_word);
	header('Location: success_join.php');
	die();
}
echo "failed?";
?>

</body>
</html>