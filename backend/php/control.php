<?php
// api/control.php — Poner en EC2 en /var/www/html/api/
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");

$host     = "TU_ENDPOINT_RDS.amazonaws.com";
$db       = "homesecurity";
$user     = "admin";
$password = "TU_PASSWORD_RDS";

$body        = json_decode(file_get_contents("php://input"), true);
$dispositivo = $body["dispositivo"] ?? "";
$estado      = $body["estado"] ?? "";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8", $user, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $stmt = $pdo->prepare("INSERT INTO controles (dispositivo, estado, fecha) VALUES (?, ?, NOW())");
    $stmt->execute([$dispositivo, $estado]);

    echo json_encode(["success" => true, "dispositivo" => $dispositivo, "estado" => $estado]);

} catch (Exception $e) {
    echo json_encode(["success" => false, "error" => $e->getMessage()]);
}
