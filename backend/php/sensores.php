<?php
// api/sensores.php — Poner en EC2 en /var/www/html/api/
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$host     = "TU_ENDPOINT_RDS.amazonaws.com";
$db       = "homesecurity";
$user     = "admin";
$password = "TU_PASSWORD_RDS";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8", $user, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Obtener último registro de sensores
    $stmt = $pdo->query("SELECT * FROM sensores ORDER BY fecha DESC LIMIT 1");
    $row  = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($row) {
        echo json_encode([
            "temperatura" => $row["temperatura"],
            "humedad"     => $row["humedad"],
            "gas"         => $row["gas"],
            "movimiento"  => $row["movimiento"],
            "fecha"       => $row["fecha"]
        ]);
    } else {
        // Sin datos aún, devuelve valores base
        echo json_encode([
            "temperatura" => "22.5",
            "humedad"     => "55",
            "gas"         => "Normal",
            "movimiento"  => "Sin mov.",
            "fecha"       => date("Y-m-d H:i:s")
        ]);
    }

} catch (Exception $e) {
    // Datos simulados si falla la BD
    echo json_encode([
        "temperatura" => "23.0",
        "humedad"     => "60",
        "gas"         => "Normal",
        "movimiento"  => "Sin mov.",
        "fecha"       => date("Y-m-d H:i:s")
    ]);
}
