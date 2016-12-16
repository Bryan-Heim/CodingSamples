<?php
session_start();
if(!isset($_SESSION['username'])){
	header('Location: homepage.php');
}
if ($_SERVER['REQUEST_METHOD'] == 'POST')
{
	$username = $_SESSION['username'];
	require ('mysqli_connect.php');
	if(empty($_POST['sbgname'])|| empty($_POST['coursename'])){
		header('Location: searcherrors.html');
	} else{
		$SGName = $_POST['sbgname'];
		$SGCourse = $_POST['coursename'];
		$SQLCommand = "SELECT * FROM StudyGroup WHERE StudyGroupName='$SGName'";
		$qRun = mysqli_query($dbc, $SQLCommand);
		if(mysqli_num_rows($qRun)!=0){
			header('Location: groupalreadyexists.html');
		} else{
			$SQLCommand = "SELECT * FROM Course WHERE CourseName='$SGCourse'";
			$qRun = mysqli_query($dbc, $SQLCommand);
			if(mysqli_num_rows($qRun)==0){
				$SQLCommand = "INSERT INTO Course (CourseName) VALUES ('$SGCourse')";
				$qRun = mysqli_query($dbc, $SQLCommand);	
			}
			$SGCourseQ = "SELECT CourseID FROM Course WHERE CourseName = '$SGCourse'";
			$coureResult = @mysqli_query($dbc, $SGCourseQ);
			while ($row = $coureResult->fetch_assoc()) {
				$theCourseId = $row["CourseID"];
			}
			$createQuery = "INSERT INTO StudyGroup (StudyGroupName, CourseID) VALUES ('$SGName','$theCourseId')";
			$runQ = @mysqli_query($dbc, $createQuery);
			if($runQ){
				$SGCourseQ = "SELECT StudyGroupID FROM StudyGroup WHERE StudyGroupName = '$SGName'";
				$coureResult = @mysqli_query($dbc, $SGCourseQ);
				while ($row = $coureResult->fetch_assoc()) {
					$theSGID = $row["StudyGroupID"];
				}
				$SGCourseQ = "SELECT UserID FROM UserLogin WHERE Username = '$username'";
				$coureResult = @mysqli_query($dbc, $SGCourseQ);
				while ($row = $coureResult->fetch_assoc()) {
					$theUserID = $row["UserID"];
				}
				$createQuery = "INSERT INTO Membership (UserID, StudyGroupID) VALUES ('$theUserID','$theSGID')";
				$qRun = mysqli_query($dbc, $SQLCommand);
				if($runQ){
				header('Location: studyGroupRedirect.php');}
			}
		}
	}
		
}	
?>