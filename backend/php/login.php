<?php
// api/login.php — Poner en EC2 en /var/www/html/api/
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");

// Conexión a RDS MySQL
$host     = "TU_ENDPOINT_RDS.amazonaws.com";
$db       = "homesecurity";
$user     = "admin";
$password = "TU_PASSWORD_RDS";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8", $user, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (Exception $e) {
    echo json_encode(["success" => false, "error" => "DB error"]);
    exit;
}

// Leer body JSON
$body    = json_decode(file_get_contents("php://input"), true);
$usuario = $body["usuario"] ?? "";
$pass    = $body["password"] ?? "";

if (empty($usuario) || empty($pass)) {
    echo json_encode(["success" => false, "error" => "Campos vacíos"]);
    exit;
}

// Buscar usuario en BD
$stmt = $pdo->prepare("SELECT password_hash FROM usuarios WHERE usuario = ? LIMIT 1");
$stmt->execute([$usuario]);
$row = $stmt->fetch(PDO::FETCH_ASSOC);

if ($row && password_verify($pass, $row["password_hash"])) {
    echo json_encode(["success" => true, "usuario" => $usuario]);
} else {
    echo json_encode(["success" => false, "error" => "Credenciales incorrectas"]);
}
