"""
Ejecutar UNA VEZ para crear usuarios con contraseña hasheada (bcrypt) en RDS.

Uso:
    export DB_HOST=... DB_USER=... DB_PASSWORD=...   (Windows: set en vez de export)
    python crear_usuario.py

Las contraseñas se piden por consola; no quedan escritas en el código.
"""
import os
import getpass
import bcrypt
import mysql.connector

DB_CONFIG = {
    "host":     os.environ["DB_HOST"],
    "port":     int(os.environ.get("DB_PORT", 3306)),
    "user":     os.environ["DB_USER"],
    "password": os.environ["DB_PASSWORD"],
    "database": os.environ.get("DB_NAME", "homesecurity"),
}

usuarios = [
    ("admin", "Administrador"),
    ("bael",  "Bael"),
]

conn   = mysql.connector.connect(**DB_CONFIG)
cursor = conn.cursor()

for usuario, nombre in usuarios:
    password = getpass.getpass(f"Contraseña para '{usuario}': ")
    hash_pw  = bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()
    try:
        cursor.execute(
            "INSERT INTO usuarios (usuario, password_hash, nombre) VALUES (%s, %s, %s)",
            (usuario, hash_pw, nombre)
        )
        print(f"Usuario '{usuario}' creado")
    except Exception as e:
        print(f"Usuario '{usuario}' ya existe o error: {e}")

conn.commit()
cursor.close()
conn.close()
print("Listo.")
