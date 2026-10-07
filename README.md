# 🏠 Home Security — App móvil Android para IoT

Proyecto de la **Unidad 2** de *Aplicaciones Móviles para IoT* (TI3042), INACAP Curicó.

Aplicación Android (Kotlin) para **monitorear sensores** y **controlar dispositivos** de un sistema de seguridad del hogar. Los usuarios y los datos se almacenan en una base de datos **MySQL en Amazon RDS**, a la que la app accede a través de una **API REST en Flask (Python)**.

**Autor:** Roberto Carlos Cerda Flores · **Docente:** Rodrigo Hernan Orellana Nuñez

## Funcionalidades

- **Login** con usuario y contraseña, validado contra la base de datos.
- **Dashboard** con temperatura, humedad, gas/humo y movimiento (PIR), con actualización automática cada 10 segundos.
- **Control de dispositivos:** relé/alarma, buzzer y servo/cerradura. Cada comando queda registrado en la base de datos.
- El dashboard indica el origen de los datos: `Sistema Activo (AWS)` cuando provienen de la base de datos.

## Arquitectura

```
 App Android (Kotlin)  ──WiFi · HTTP/JSON──▶  API Flask (puerto 5000)  ──▶  MySQL en Amazon RDS
   Login · Dashboard                           backend/app.py                 usuarios · sensores
   Control                                                                    controles
```

La app **nunca se conecta directo a la base de datos**: todo pasa por la API. La app y la API se comunican por la red WiFi local, por lo que la app se puede ejecutar en dos o más dispositivos Android conectados a la misma red.

## Tecnologías

| Capa | Tecnología |
|---|---|
| App móvil | Android Studio, Kotlin, Coroutines, Material Components |
| API | Flask (Python), flask-cors |
| Base de datos | MySQL en Amazon RDS |
| Seguridad | bcrypt (hash de contraseñas), consultas parametrizadas |

## Estructura del repositorio

```
android/   Proyecto Android Studio (Kotlin)
backend/
  app.py              API REST en Flask
  crear_usuario.py    crea usuarios con contraseña hasheada
  database.sql        esquema de la base de datos
  requirements.txt    dependencias de Python
  .env.example        variables de entorno necesarias
  php/                versión alternativa de los endpoints en PHP (no usada en la demo)
```

## Base de datos

Tablas: `usuarios` (contraseñas con hash bcrypt), `sensores` (lecturas) y `controles` (historial de comandos). El esquema está en `backend/database.sql`.

## Cómo ejecutarlo

1. **Base de datos:** crea una instancia MySQL en Amazon RDS y ejecuta `backend/database.sql`.
2. **Variables de entorno:** define `DB_HOST`, `DB_USER`, `DB_PASSWORD` y `DB_NAME` (ver `backend/.env.example`).
3. **Usuarios:** ejecuta `python backend/crear_usuario.py` para crear usuarios con contraseña hasheada.
4. **API:** `pip install -r backend/requirements.txt` y luego `python backend/app.py`.
5. **App:** abre `android/` en Android Studio y ajusta en las actividades la IP del equipo que ejecuta Flask (`http://IP:5000/api/...`).
6. Ejecuta la app en dos dispositivos Android (físicos o emuladores) conectados a la misma red que la API.

## Seguridad (lineamientos ISO/IEC 27400)

- Contraseñas almacenadas **hasheadas con bcrypt**, nunca en texto plano.
- Consultas SQL **parametrizadas** (protección contra inyección SQL).
- La app **no accede directamente** a la base de datos; solo a través de la API.
- Credenciales de AWS **fuera del repositorio** (variables de entorno).
- Sin conexión a la API, el login se rechaza (no hay credenciales de respaldo en la app).
- **Limitación conocida:** la comunicación app–API usa HTTP. Como mejora se contempla HTTPS/TLS con certificado.
