<?php
session_start();
	
if(isset($_SESSION['quizflag']))	// check if first question has been asked, if not go to else
{
	$_SESSION['question_counter']++;	// add one to indicate current question
	if(isset($_POST['picked']))
	{
		if ( (int)$_SESSION['correct_index']==(int)$_POST['picked'] )
		{
			$_SESSION['total_correct']++;	// user picked correct for that question
			$_SESSION['tot_ppl_corr']++;
		}
		else
		{
			$_SESSION['total_incorrect']++;	// user incorrect for that question
			$_SESSION['tot_ppl_incorr']++;
		}
	}
	else
	{
		$_SESSION['total_incorrect']++;	// user didnt answer and clicked submit, just add to wrong
		$_SESSION['tot_ppl_incorr']++;
	}
	while($_SESSION['question_counter'] < $_SESSION['question_max']) // will loop through all questions
	{
		$quizpath = $_SESSION['quiz_path'];	// local variable of the quiz path
		$quiz_chop = new SplFileObject("$quizpath");	// open the quiz#.txt
		$quiz_chop->seek($_SESSION['question_counter']);	// go to the current question
		$picked_question = $quiz_chop->current();		// grab that line
		$_SESSION['question_2_display'] = $picked_question;
		header('Location: display_question.php');	// send question to be displayed and come back here to check
		die();
	}
	$_SESSION['tot_ppl_taken']++;	// all questions done and can now added to total user
	$num_takers = $_SESSION['tot_ppl_taken'];
	$tcorrect = $_SESSION['tot_ppl_corr'];
	$tincorrect = $_SESSION['tot_ppl_incorr'];
	$all_quizes = new SplFileObject('allquizzes.txt');	// used to original try and put line back
	$all_quizes->seek($_SESSION['quiz_num']);			// if comment out, get php error and have to change quizzes filename
	$_SESSION['question_counter'] = 0;	// for error checking
	unset($_SESSION['quizflag']);	// error checking
	$fp = fopen("dailystats.txt", "w");
	fwrite( $fp, "$num_takers#$tcorrect#$tincorrect");	// writes out updated daily stats
	fclose($fp);
	header('Location: final_results.php');	// send to results
	die();
}
else
{
$temp = $_SESSION['quiz_num'];	
$quiz_num = intval($temp);	// get the quiz num chosen from day.txt and set as int
$all_quizes = new SplFileObject('allquizzes.txt');
$all_quizes->seek($quiz_num);
$picked_quiz = $all_quizes->current();	// open quizzes.txt and chose the quiz#.txt given from random number
$tempholder = explode("#",$picked_quiz);	

$_SESSION['total_correct'] = 0;	// for the users scores
$_SESSION['total_incorrect'] = 0;
$_SESSION['question_counter'] = 0;
$_SESSION['quiz_path'] = $tempholder[0];	// incase anything needs access to which quiz#.txt exactly
$_SESSION['question_max'] = (int)$tempholder[1]; // max num of questions for loop above
$_SESSION['quizflag'] = true;	// now quiz is ready to be checked once we revisit this page
$quizpath = $tempholder[0];
$quiz_chop = new SplFileObject("$quizpath");
$quiz_chop->seek(0);
$picked_question = $quiz_chop->current();	// open up the quiz#.txt and get the question on the first line and send to display
$_SESSION['question_2_display'] = $picked_question;
header('Location: display_question.php');
die();
}
?>