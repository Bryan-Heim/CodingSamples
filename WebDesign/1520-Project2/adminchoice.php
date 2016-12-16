<?php
session_start();
if (!isset($_SESSION['username']))
	header("Location: login.php");
if(!isset($_POST['choice']))
	header("Location: ticket_info.php"); // gets the ticket id that was selected
else
{
	$username = $_SESSION['username'];
	$ticketid = $_SESSION['idpicked'];
	$choice = $_POST['choice'];
	$myconnection = new mysqli("localhost", "root", "", "Project2");
	if($choice == 'bck')
		header("Location: show_open_tickets.php");	// if they pick go back, goes to show all tickets
	if($choice == 'del')
	{
		$query = "DELETE FROM tickets WHERE id='$ticketid'";	// deletes the ticket from the table
		mysqli_query($myconnection, $query);
		unset($query);
		$query = "DELETE FROM ticket_status WHERE id='$ticketid'";	// deletes the ticket status
		mysqli_query($myconnection, $query);
		header("Location: show_open_tickets.php");
	}
	if($choice == 'ooc')
	{
		$query = "SELECT * FROM ticket_status WHERE id='$ticketid'";
		$result = mysqli_query($myconnection, $query);
		$row = mysqli_fetch_assoc($result);
		$cur_stat = $row['current_status'];
		if(strncmp($cur_stat, 'open',4)==0)	// if the ticket picked is currently oppened
		{
			$query2 = "UPDATE ticket_status SET current_status='closed' WHERE id='$ticketid'";	//set the ticket to closed
			mysqli_query($myconnection, $query2);
			$query3 = "SELECT semail FROM tickets WHERE id='$ticketid'";
			$result2 = mysqli_query($myconnection, $query3);
			$row2 = mysqli_fetch_assoc($result2);
			$reciever = $row2['semail'];	// send the sender an email saying ticket was now closed
			$subject = "Ticket Closed!";
			$body = "An administrator has closed your ticket. The problem should be fixed!";
			mail($reciever,$subject,$body);
			header("Location: ticket_info.php");
		}
		else
		{
			$query2 = "UPDATE ticket_status SET current_status='open' WHERE id='$ticketid'";
			mysqli_query($myconnection, $query2);	// else ticket was closed and turn it back to open
			header("Location: ticket_info.php");
		}
	}
	if($choice == 'aft')
	{
		$query = "SELECT * FROM ticket_status WHERE id='$ticketid'";
		$result = mysqli_query($myconnection, $query);
		$row = mysqli_fetch_assoc($result);
		$admin_cur = $row['admin_name'];
		if(strncmp($admin_cur, 'Unassigned',10)==0)	// if ticket is unassigned, assigns the current user to it
		{
			$query2 = "UPDATE ticket_status SET admin_name='$username' WHERE id='$ticketid'";
			mysqli_query($myconnection, $query2);
			header("Location: ticket_info.php");
		}
		else
			header("Location: ticket_info.php");// ticket already is assigned go back
	}
	if($choice == 'rft')
	{
		$query = "SELECT * FROM ticket_status WHERE id='$ticketid'";
		$result = mysqli_query($myconnection, $query);
		$row = mysqli_fetch_assoc($result);
		$admin_cur = $row['admin_name'];
		if($admin_cur == $username)	// if the user is the admin of that ticket
		{
			$query2 = "UPDATE ticket_status SET admin_name='Unassigned' WHERE id='$ticketid'";
			mysqli_query($myconnection, $query2);
			header("Location: ticket_info.php");	// will make ticket unassigned
		}
		else
			header("Location: ticket_info.php");	// user logged in isnt assigned ticket, so take back
	}
	if($choice == 'mst')
	{
		$query = "SELECT * FROM tickets WHERE id='$ticketid'";
		$result = mysqli_query($myconnection, $query);
		$row = mysqli_fetch_assoc($result);
		$_SESSION['sent_name'] = $row['sname'];
		header("Location: show_senders_tickets.php");	// get the sender name and go to show only their tickets
	}
	if($choice == 'ems')
		header("Location: emailsender.php");	// to the email sender
	if($choice == 'sim')
		header("Location: similar_find.php");
}

die();
?>