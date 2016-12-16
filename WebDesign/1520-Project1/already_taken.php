<html>
<title>Redirecting</title>
<meta http-equiv="refresh" 
content="15;URL=http://localhost/bph11_Project1/home.php">
</head>
<style>
body {font-family: Arial, Helvetica, sans-serif;}
</style>
<body bgcolor=#d0ffd6>
<center>

<br><br><br><br>

<h1> <b>Sorry!</b> You already took todays Quiz. </h1>
<br>
<h3> Please Try Again Tomorrow!  </h3>

<?php
setcookie('username', 0, time() - 3600); // sets to an hour ago, gets rid of auto-login
die();
?>

</center>
</body>
</html>