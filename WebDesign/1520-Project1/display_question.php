<?php
session_start();
if (!(isset($_SESSION['username'])))
	header('Location: home.php'); // redundant but no session estalished, not logged in
?>

<html>
<style>
body {font-family: Arial, Helvetica, sans-serif;}
</style>
<body bgcolor=#d0ffd6>
<center>

<br><br><br><br>

<h1> Good Luck! </h1>
<br>
<?php
$tempholder = explode("#",$_SESSION['question_2_display']);		// display the question
$question = $tempholder[0];
echo "<h3> $question </h3><br>";
$choice_holder = explode(":",$tempholder[1]); // will hold the answer choices
$_SESSION['correct_index'] = (int)$tempholder[2];	// setup the correct valued index for display_quiz.php
?>

<form action="display_quiz.php" method="post">
<input type="radio" name="picked" value="0"> 
<?php 
$question1 = $choice_holder[0];
echo "$question1";
?>
<br>
<input type="radio" name="picked" value="1"> 
<?php 
$question2 = $choice_holder[1];			// used to print questions. noticed that holder[] worked so i left it during debug
echo "$question2"; 
?>
<br>	
<input type="radio" name="picked" value="2"> <?php echo "$choice_holder[2]" ?>
<br>
<input type="radio" name="picked" value="3"> <?php echo "$choice_holder[3]" ?>
<br>
<input type="submit" value="Submit">
</form>

</center>
</body>
</html>