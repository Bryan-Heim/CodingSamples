<?php
session_start();
if(!isset($_SESSION['username'])){
	header('Location: homepage.php');
}
if ($_SERVER['REQUEST_METHOD'] == 'POST')
{
	require ('mysqli_connect.php');
	if(empty($_POST['sbgnamesearch'])&& empty($_POST['coursenamesearch'])){
		header('Location: searcherrors.html');
	} else if(!empty($_POST['sbgnamesearch'])&& empty($_POST['coursenamesearch'])){
		$SGSearch = $_POST['sbgnamesearch'];
		$SQLCommand = "SELECT * FROM StudyGroup WHERE StudyGroupName = '$SGSearch'";
		$qRun = @mysqli_query($dbc, $SQLCommand);
		if(mysqli_num_rows($qRun)==0){
			header('Location: nogroupsincourse.html');
		} else{
			$x=0;
			$array = array();
			while ($row = mysqli_fetch_array($qRun)) {
				$array[$x]= $row["StudyGroupName"];
				$x = $x+1;
			}
			$_SESSION['results']=$array;
			header('Location: groupsResults.php');
		}
	} else {
		$SGCourse = $_POST['coursenamesearch'];
		$SGCourseQ = "SELECT CourseID FROM Course WHERE CourseName = '$SGCourse'";
		$coureResult = @mysqli_query($dbc, $SGCourseQ);
		while ($row = $coureResult->fetch_assoc()) {
				$theCourseId = $row["CourseID"];
		}
		$SQLCommand = "SELECT * FROM StudyGroup WHERE CourseID='$theCourseId'";
		$qRun = @mysqli_query($dbc, $SQLCommand);
		//$statement = mysqli_num_rows($qRun);
		//echo "$statement";
		if(mysqli_num_rows($qRun)==0){
			header('Location: nogroupsincourse.html');
		} else{
			$x=0;
			$array = array();
			while ($row = mysqli_fetch_array($qRun)) {
				$array[$x]= $row["StudyGroupName"];
				//echo $row["StudyGroupName"].$array[$x];
				$x = $x+1;
			}
			$_SESSION['results']=$array;
			header('Location: groupsResults.php');
		}

	}
}	
?>