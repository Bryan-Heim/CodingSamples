<?php
session_start();
?>
<html>
<title>Redirecting</title>
<meta http-equiv="refresh" 
content="60;URL=http://localhost/bph11_Project1/goodbye.php">
</head>
<style>
body {font-family: Arial, Helvetica, sans-serif;}
</style>
<body bgcolor=#d0ffd6>
<center>

<br><br><br><br>

<h1> And Here Are Your Results! </h1>

<br>
<?php
$people_taken = $_SESSION['tot_ppl_taken'];
$tot_correct = $_SESSION['total_correct'];
$tot_ppl_correct = $_SESSION['tot_ppl_corr'];	// setup to display final results
$tot_ppl_incorrect = $_SESSION['tot_ppl_incorr'];
$tot_max = $_SESSION['question_max'];
$average_correct = ($tot_ppl_correct/($tot_ppl_correct+$tot_ppl_incorrect))*100;	// get total users average
$total_taken = ($tot_ppl_correct+$tot_ppl_incorrect);	
$average_user = ($tot_correct/$tot_max)*100;	// current users average
echo "<h2>You have answered a total of $tot_correct out of $tot_max correct!</h2>";
echo "<h2>Your total percent correct is: $average_user!</h2><br>";
echo "<h2>Everyone who took the quiz got $tot_ppl_correct out of $total_taken correct!</h2>";
echo "<h2>The total correct from everyone is $average_correct%!</h2></br>";
echo "<h4> <a href=\"http://localhost/bph11_Project1/goodbye.php\">Click here if you are done.</a> </h4>";
?>



</center>
</body>
</html>