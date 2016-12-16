<?php # Script 0.0 - mysqli_connect.php

	DEFINE ('DB_USER', 'name-here');
	DEFINE ('DB_PASSWORD', 'password-here');
	DEFINE ('DB_HOST', 'host-url-here');
	DEFINE ('DB_NAME', 'database-name-here');
	$dbc = @mysqli_connect (DB_HOST, DB_USER, DB_PASSWORD, DB_NAME) OR die ('Couldnt Connect:'.mysqli_connect_error( ));
	mysqli_set_charset($dbc, 'utf8');
?>