<?php

/* SERVER VARIABLES */
$servername = "62.149.150.110";
$username = "Sql321130";
$password = "da56b9ac";
$dbname = "Sql321130_1";


/**
 * GET ALL PHONE NUMBERS FROM DATABASE
 **/
function getPhones() {

	global $servername, $username, $password, $dbname;

/*  	
	$servername = "localhost";
	$username = "root";
	$password = "";
	$dbname = "firebasetokendb";
	$results = "";
*/
/*
	$servername = "62.149.150.110";
	$username = "Sql321130";
	$password = "da56b9ac";
	$dbname = "Sql321130_1";
*/
	$results = "";



	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	// Check connection
	if ($conn->connect_error) {
	    die("Connection failed: " . $conn->connect_error);
	} 

	$sql = "SELECT user_phone, user_name FROM data";
	$result = $conn->query($sql);

	if ($result->num_rows > 0) {
	    // fill result array
		$results = array();
		$i = 0;
	    while($row = $result->fetch_assoc()) {
	        $results[$i++] = $row["user_phone"] . " - " . stripslashes($row["user_name"]);
	    }

	} else {
	    $results = false;
	}

	$conn->close();

	return $results;
}

/**
 * UPDATE IF PHONE EXISTS OR INSERT DATA IN DATABASE
 **/
function storeData($tokenID, $userPhone, $userName) {
  	
	$msg = "";
	$result = "";

	$result = getTokenIDByPhone($userPhone);

	if(!$result){
		//insert
		$msg = insertData($tokenID, $userPhone, $userName);
	} else {
		//update
		$msg = updateData($tokenID, $userPhone, $userName);
	}
	
	return $msg;
}

/**
 * GET PHONE ASSOCIATED TOKEN FROM DATABASE
 **/
function getTokenIDByPhone($userPhone) {
  	
	global $servername, $username, $password, $dbname;

	$results = "";

	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	// Check connection
	if ($conn->connect_error) {
	    die("Connection failed: " . $conn->connect_error);
	} 

	$sql = "SELECT user_phone, token FROM data WHERE user_phone='$userPhone'";
	$result = $conn->query($sql);

	if ($result->num_rows > 0) {
	    // fill result array
		$results = array();
		$i = 0;
	    while($row = $result->fetch_assoc()) {
	        $results[$i++] = array($row["user_phone"] => $row["token"]);
	    }

	} else {
	    $results = false;
	}

	$conn->close();

	return $results;
}

/**
 * SEND FIREBASE NOTIFICATION FUNCTION
 **/
function sendNotify($tokenID, $serverTokenID) {
  	
	$url = "https://fcm.googleapis.com/fcm/send";
	//non è importante il contenuto
	$message = "Hello World";

	//setting http header
	$headers = array (
            "Authorization: key=$serverTokenID",
            "Content-Type: application/json"
    );

	//setting data
    $data = array (
                    "message" => $message
            );
    //$data = json_encode ( $data );
	
    //setting notification data
    $fields = array (
            'to' => trim($tokenID),
            'data' => $data
    );
    $fields = json_encode ( $fields );

    //send data to server
    // use key 'http' even if you send the request to https://...
	$options = array(
	    'http' => array(
	        'header'  => $headers,
	        'method'  => 'POST',
	        'content' => $fields
	    )
	);
	$context  = stream_context_create( $options );
	$result = file_get_contents($url, false, $context);
	
  	return $result;
}

/**
 * GET ALL TOKENS FROM DB
 **/
function getAllTokensID() {
  	
	global $servername, $username, $password, $dbname;

	$results = "";

	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	// Check connection
	if ($conn->connect_error) {
	    die("Connection failed: " . $conn->connect_error);
	} 

	$sql = "SELECT user_phone, token FROM data";
	$result = $conn->query($sql);

	if ($result->num_rows > 0) {
	    // fill result array
		$results = array();
		$i = 0;
	    while($row = $result->fetch_assoc()) {
	        $results[$i++] = array($row["user_phone"] => $row["token"]);
	    }

	} else {
	    $results = false;
	}

	$conn->close();

	return $results;
}


/**
 * DATABASE EDIT FUNCTIONS
 **/
function insertData($tokenID, $userPhone, $userName){

	global $servername, $username, $password, $dbname;

	$msg = "";

	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	// Check connection
	if ($conn->connect_error) {
	    die("Connection failed: " . $conn->connect_error);
	} 

	$sql = "INSERT INTO data (user_phone, user_name, token) VALUES ('$userPhone', '$userName', '$tokenID')";

	if ($conn->query($sql) === TRUE) {
	    $msg = true;
	} else {
	    $msg = "Error: " . $sql . "<br>" . $conn->error;
	}

	$conn->close();

	return $msg;
}


function updateData($tokenID, $userPhone, $userName){

	global $servername, $username, $password, $dbname;
	
	$msg = "";

	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	// Check connection
	if ($conn->connect_error) {
	    die("Connection failed: " . $conn->connect_error);
	} 

	$sql = "UPDATE data SET user_phone='$userPhone', user_name='$userName', token='$tokenID' WHERE user_phone='$userPhone'";

	if ($conn->query($sql) === TRUE) {
	    $msg = true;
	} else {
	    $msg = "Error updating record: " . $conn->error;
	}

	$conn->close();

	return $msg;
}

?>
