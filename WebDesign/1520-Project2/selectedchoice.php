<?php
session_start();
// this script simply gets the choice that an admin selected and will redirect to the appropraite script
if(!isset($_SESSION['username']))
	header("Location: management.php");
if(isset($_POST['sortby']))		// used to check and set the sortby options to be used in the future
{
	$sortby = $_POST['sortby'];
	if ($sortby == 'ticketid')
		$_SESSION['sortby'] = 'id';
	if ($sortby == 'date')
		$_SESSION['sortby'] = 'date';
	if ($sortby == 'sname')
		$_SESSION['sortby'] = 'sname';
	if ($sortby == 'email')
		$_SESSION['sortby'] = 'semail';
	if ($sortby == 'subject')
		$_SESSION['sortby'] = 'ssubject';
}
if(isset($_POST['ticketpicked']))
	$_SESSION['idpicked'] = $_POST['ticketpicked'];
if(isset($_POST['open']))
	header("Location: show_open_tickets.php");
if(isset($_POST['val']))
	header("Location: show_all_tickets.php");
if(isset($_POST['sort']))
{
	header("Location: sortby.php");
}
if(isset($_POST['vst']))
{	
	if (isset($_POST['ticketpicked']))
	{
		$fun = $_POST['ticketpicked'];
		$_SESSION['idpicked'] = $fun;
	}
	header("Location: ticket_info.php");
}
if(isset($_POST['vmt']))
	header("Location: show_my_tickets.php");
if(isset($_POST['vut']))
	header("Location: show_una_tickets.php");
if(isset($_POST['logo']))
{
	session_destroy();
	header("Location: login.php");
}
die();


?>