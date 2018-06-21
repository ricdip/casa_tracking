<?php  

error_reporting(E_ALL);
ini_set('display_errors', '1');

require_once('/functions/functions.php'); 
$SERVER_TOKEN_ID = "AAAApPUz3bw:APA91bGEbKrK5gfIWy8yJywUzirGEHZGBXlIr3QCUWvFhK-Yb8H7wXqQ80IEhKLb9QacRGs3gFYLr8NA2GxqbwbWJ68DE777MzVsEylH0dl52AwJ6zR3zjNxOaTmNqYXLyPpIogedr1k" ;

$result = '';

if(isset($_GET["task"])) 
	$task = $_GET["task"];

if(isset($_POST["task"])) 
	$task = $_POST["task"];

if(!empty($task)){

	switch ($task) {
		case 'send_notify':
			if(isset($_GET["phone"])){
				$phone = $_GET["phone"];
				$response = getTokenIDByPhone($phone);
				$tokenID = getTokenIDByPhone($phone)[0][$phone];

				$result = sendNotify($tokenID, $SERVER_TOKEN_ID);
			}
			break;

		case 'store_data':
			if(isset($_POST["token"]) && isset($_POST["phone"]) &&  isset($_POST["userName"])){
				$token = $_POST["token"];
				$phone = $_POST["phone"];
				$userName = $_POST["userName"];

				$result = storeData($token, $phone, $userName);
			}
			break;

		case 'get_phones':

			$result = getPhones();
			break;
		
		default:
			break;
	}
}

$return = json_encode($result);

echo $return;

?>
