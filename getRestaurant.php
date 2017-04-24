<?php
header('Content-Type: application/json; charset=UTF-8');
$mJSON = file_get_contents("php://input");
$POST = json_decode($mJSON, true);

if(isset($POST['city']))
{
	$city = $POST['city'];

	require 'conf.php';

	$data = array();
	$sql = "SELECT Name, Address, Lat, Lng FROM Restaurants WHERE City = '$city'";
	$result = $conn->query($sql);
	if($result)
	{
		while($row = $result->fetch_row())
		{
			array_push($data, $row);
		}
	}
	echo json_encode(array("Code" => 1, "Data" => $data));
}