# 🏠 Home Security — App móvil Android para IoT

Proyecto de la **Unidad 2** de *Aplicaciones Móviles para IoT* (TI3042), INACAP Curicó.

Aplicación Android (Kotlin) para **monitorear sensores** y **controlar dispositivos** de un sistema de seguridad del hogar. Los datos y los usuarios se almacenan en una base de datos **MySQL en Amazon RDS**, a la que la app accede a través de una API alojada en **Amazon EC2**.

**Autor:** [Roberto Carlos Cerda Flores] · **Docente:** [Rodrigo Hernan Orellana Nuñez]

## Funcionalidades

- **Login** con usuario y contraseña (validación contra la base de datos).
- **Dashboard** con temperatura, humedad, gas/humo y movimiento (PIR), con actualización automática cada 10 segundos.
- **Control de dispositivos:** relé/alarma, buzzer y servo/cerradura. Cada comando queda registrado en la base de datos.
- El dashboard indica el origen de los datos: `Sistema Activo (AWS)` cuando provienen de la base de datos.

## Arquitectura

```
 App Android (Kotlin)  ──HTTP/JSON──▶  API en EC2 (PHP)  ──▶  MySQL en Amazon RDS
   Login · Dashboard                 login.php · sensores.php      usuarios · sensores
   Control                           control.php                   controles
```

La app **nunca se conecta directo a la base de datos**: todo pasa por la API.
Además de los endpoints PHP, el repositorio incluye una versión equivalente en Flask (`backend/app.py`).

## Tecnologías

| Capa | Tecnología |
|---|---|
| App móvil | Android Studio, Kotlin, Coroutines, Material Components |
| API | PHP (PDO) en Amazon EC2 · alternativa Flask (Python) |
| Base de datos | MySQL en Amazon RDS |
| Seguridad | bcrypt (hash de contraseñas), consultas preparadas |

## Estructura del repositorio

```
android/   Proyecto Android Studio (Kotlin)
backend/
  php/                login.php, sensores.php, control.php
  app.py              API alternativa en Flask
  crear_usuario.py    crea usuarios con contraseña hasheada
  database.sql        esquema de la base de datos
  .env.example        variables de entorno necesarias
```

## Base de datos

Tablas: `usuarios` (contraseñas con hash bcrypt), `sensores` (lecturas) y `controles` (historial de comandos). El esquema está en `backend/database.sql`.

## Cómo ejecutarlo

1. **Base de datos:** crea una instancia MySQL en RDS y ejecuta `backend/database.sql`.
2. **Usuarios:** configura las variables de `backend/.env.example` y ejecuta `python crear_usuario.py`.
3. **API:** copia los `.php` a `/var/www/html/api/` en EC2 y completa tus credenciales de RDS en el servidor (o ejecuta `app.py` con las variables de entorno).
4. **App:** abre `android/` en Android Studio y reemplaza `TU_IP_EC2` por la IP de tu servidor en `LoginActivity.kt`, `DashboardActivity.kt` y `ControlActivity.kt`.
5. Ejecuta la app en dos dispositivos Android (físicos o emuladores) conectados a internet.

## Seguridad (lineamientos ISO/IEC 27400)

- Contraseñas almacenadas **hasheadas con bcrypt**, nunca en texto plano.
- Consultas SQL **preparadas** (protección contra inyección SQL).
- La app **no accede directamente** a la base de datos; solo a través de la API.
- Credenciales de AWS **fuera del repositorio** (variables de entorno / placeholders).
- Sin conexión a la API, el login se rechaza (no hay credenciales de respaldo en la app).
- **Limitación conocida:** la comunicación app–API usa HTTP. Como mejora se contempla HTTPS/TLS con un dominio y certificado.
