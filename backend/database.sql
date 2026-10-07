-- Ejecutar en MySQL RDS
CREATE DATABASE IF NOT EXISTS homesecurity CHARACTER SET utf8 COLLATE utf8_general_ci;
USE homesecurity;

-- Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    usuario       VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre        VARCHAR(100),
    creado_en     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sensores
CREATE TABLE IF NOT EXISTS sensores (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    temperatura DECIMAL(5,2),
    humedad     DECIMAL(5,2),
    gas         VARCHAR(20) DEFAULT 'Normal',
    movimiento  VARCHAR(20) DEFAULT 'Sin mov.',
    fecha       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Controles
CREATE TABLE IF NOT EXISTS controles (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    dispositivo VARCHAR(50),
    estado      VARCHAR(20),
    fecha       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Los usuarios se crean con backend/crear_usuario.py (contraseñas hasheadas con bcrypt).

-- Datos iniciales sensores
INSERT INTO sensores (temperatura, humedad, gas, movimiento) VALUES
(22.5, 55.0, 'Normal', 'Sin mov.'),
(23.1, 52.0, 'Normal', 'Detectado');
