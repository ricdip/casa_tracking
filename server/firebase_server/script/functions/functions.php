<?php

function getPhones() {
  	
	$servername = "localhost";
	$username = "root";
	$password = "";
	$dbname = "firebasetokendb";
	$results = "";

	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	// Check connection
	if ($conn->connect_error) {
	    die("Connection failed: " . $conn->connect_error);
	} 

	$sql = "SELECT user_phone FROM data";
	$result = $conn->query($sql);

	if ($result->num_rows > 0) {
	    // fill result array
		$results = array();
		$i = 0;
	    while($row = $result->fetch_assoc()) {
	        $results[$i++] = $row["user_phone"];
	    }

	} else {
	    $results = false;
	}

	$conn->close();

	return $results;
}

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

function getTokenIDByPhone($userPhone) {
  	
	$servername = "localhost";
	$username = "root";
	$password = "";
	$dbname = "firebasetokendb";
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

function sendNotify($tokenID, $serverTokenID) {
  	
	$url = "https://fcm.googleapis.com/fcm/send";
	//non è importante il contenuto
	$message = "Hello World";

	//setting http header
	$headers = array (
            'Authorization: key=' . "$serverTokenID",
            'Content-Type: application/json'
    );

	//setting data
    $data = array (
                    "message" => $message
            );
    $data = json_encode ( $data );
	
    //setting notification data
    /*
    $fields = array (
            'to' => array (
                    trim($tokenID)
            ),
            'data' => array (
                    $data
            )
    );*/
    $fields = array (
            'to' => trim($tokenID),
            'data' => $data
    );

    $fields = json_encode ( $fields );

    //send data to server
    $ch = curl_init ();
    curl_setopt ( $ch, CURLOPT_URL, $url );
    curl_setopt ( $ch, CURLOPT_POST, true );
    curl_setopt ( $ch, CURLOPT_HTTPHEADER, $headers );
    curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, true );
    curl_setopt ( $ch, CURLOPT_POSTFIELDS, $fields );

    $result = curl_exec ( $ch );
    //echo $result;
    curl_close ( $ch );

  	return $result;
}


function insertData($tokenID, $userPhone, $userName){

	$servername = "localhost";
	$username = "root";
	$password = "";
	$dbname = "firebasetokendb";
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

	$servername = "localhost";
	$username = "root";
	$password = "";
	$dbname = "firebasetokendb";
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
