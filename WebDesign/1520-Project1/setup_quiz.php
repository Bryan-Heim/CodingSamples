<?php
session_start();
if (!(isset($_SESSION['username'])))
	header('Location: home.php');	// getting repetitive, but no session go home
	
$fp = fopen("day.txt", "r+");	// opens days which sets up the quiz num for that day for everyone
$string_holder = fgets($fp);	//grab the latest Day#Num pair
$tempholder = explode("#",$string_holder);
$date = date('D');
$fp2 = fopen("used_quizzes.txt", "r+"); // file that contains the index of already chosen quiz so no repeated quiz's
unset($string_holder); // frees string holder to get next input
$counter = 0;
while (!feof($fp2))	// reused code from register_new_mem to loop thru and gather each line
{
	$string_holder[$counter] = fgets($fp2);
	$counter++;
}

if (!($date == $tempholder[0])) // if the day found in file isn't today, pick new quiz
{	
	$checker = false;
	while ($checker == false)
	{
		if ($checker == false)
		{
			$random_quiz_number = rand(0,6);
			$checker = true;	// sets the random number to true - meaning not used - until after foreach
			foreach( $string_holder as $value) // will loop all the preused indexes
			{
				if ($random_quiz_number == (int)$value)	// if an index is found set false
					$checker == false;
			}	// if you make it here and checker is true, then you know the index wasnt found
			$int_to_write = "$random_quiz_number"."\r\n";
			$store_int = $int_to_write.file_get_contents("used_quizzes.txt");	// write the newly used quiz index into used file
			file_put_contents("used_quizzes.txt", $store_int);	//write out to the file
		}	
	}
	$string_to_write = "$date"."#"."$random_quiz_number"."\r\n";
	$store_word = $string_to_write.file_get_contents("day.txt");	// take today and new quiz and append to top of day.txt
	file_put_contents("day.txt", $store_word);	//write out to the file
	$fp3 = fopen("dailystats.txt","w+");
	fwrite($fp3,'0#0#0');	// opens and resets the daily stats to zero for the new quiz
	fclose($fp3);
}
fclose($fp);
fclose($fp2);
$username = $_SESSION['username'];	// again setup local username variable
if(isset($_COOKIE["day_taken"][$username]))	// index their username so if user switches they will have different cookie
{											// and newly signed in user can take quiz
	if(($_COOKIE["day_taken"][$username]) == $date)	// if they have a day cookie at the index of their username and its today
	{
		header('Location: already_taken.php'); // already took
		die();
	}
}
setcookie("day_taken[$username]",$date, time() + (86400 * 30), "/");	// set their cookie for their username to the current day
$_SESSION['quiz_num'] = intval($tempholder[1]); // let the rest of session know which quiz its doing
												// this means that if they dont answer any quiz questions and leave, they cant 
												// come back to take the quiz. this is the quiz setup after all
$fp = fopen("dailystats.txt","r+");
$holder = fgets($fp);
$tempholder = explode("#",$holder);
$_SESSION['tot_ppl_taken'] = (int)$tempholder[0];	// get the total users for that days stats
$_SESSION['tot_ppl_corr'] = (int)$tempholder[1];	// stored in the form found in dailystats.txt
$_SESSION['tot_ppl_incorr'] = (int)$tempholder[2];	// kept seperate from allquizzes.txt becasue
fclose($fp);										// the quiz wont repeat after that day is over


unset($_SESSION['quizflag']);	// just incase of error, used in display_quiz.php
header('Location: display_quiz.php');
die();

?>