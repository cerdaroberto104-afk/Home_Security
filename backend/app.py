import os
from flask import Flask, request, jsonify
from flask_cors import CORS
import mysql.connector
import bcrypt
from datetime import datetime

app = Flask(__name__)
CORS(app)

# ── Configuración RDS (se lee desde variables de entorno, NUNCA en el código) ──
DB_CONFIG = {
    "host":     os.environ["DB_HOST"],
    "port":     int(os.environ.get("DB_PORT", 3306)),
    "user":     os.environ["DB_USER"],
    "password": os.environ["DB_PASSWORD"],
    "database": os.environ.get("DB_NAME", "homesecurity"),
    "ssl_disabled": False
}

def get_db():
    return mysql.connector.connect(**DB_CONFIG)

# ── LOGIN ──────────────────────────────────────────────────────
@app.route("/api/login", methods=["POST"])
def login():
    data     = request.get_json()
    usuario  = data.get("usuario", "")
    password = data.get("password", "")

    if not usuario or not password:
        return jsonify({"success": False, "error": "Campos vacíos"}), 400

    try:
        conn   = get_db()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM usuarios WHERE usuario = %s LIMIT 1", (usuario,))
        row = cursor.fetchone()
        cursor.close()
        conn.close()

        if row and bcrypt.checkpw(password.encode(), row["password_hash"].encode()):
            return jsonify({"success": True, "usuario": usuario})
        else:
            return jsonify({"success": False, "error": "Credenciales incorrectas"})

    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

# ── SENSORES — obtener último registro ────────────────────────
@app.route("/api/sensores", methods=["GET"])
def sensores():
    try:
        conn   = get_db()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM sensores ORDER BY fecha DESC LIMIT 1")
        row = cursor.fetchone()
        cursor.close()
        conn.close()

        if row:
            return jsonify({
                "temperatura": str(row["temperatura"]),
                "humedad":     str(row["humedad"]),
                "gas":         row["gas"],
                "movimiento":  row["movimiento"],
                "fecha":       str(row["fecha"])
            })
        else:
            return jsonify({
                "temperatura": "22.5",
                "humedad":     "55",
                "gas":         "Normal",
                "movimiento":  "Sin mov.",
                "fecha":       str(datetime.now())
            })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ── SENSORES — insertar nuevo registro ────────────────────────
@app.route("/api/sensores", methods=["POST"])
def insertar_sensor():
    data = request.get_json()
    try:
        conn   = get_db()
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO sensores (temperatura, humedad, gas, movimiento) VALUES (%s, %s, %s, %s)",
            (data.get("temperatura"), data.get("humedad"),
             data.get("gas", "Normal"), data.get("movimiento", "Sin mov."))
        )
        conn.commit()
        cursor.close()
        conn.close()
        return jsonify({"success": True})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

# ── CONTROL de dispositivos ────────────────────────────────────
@app.route("/api/control", methods=["POST"])
def control():
    data        = request.get_json()
    dispositivo = data.get("dispositivo", "")
    estado      = data.get("estado", "")

    try:
        conn   = get_db()
        cursor = conn.cursor()
        cursor.execute(
            "INSERT INTO controles (dispositivo, estado) VALUES (%s, %s)",
            (dispositivo, estado)
        )
        conn.commit()
        cursor.close()
        conn.close()
        return jsonify({"success": True, "dispositivo": dispositivo, "estado": estado})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

# ── HISTORIAL de controles ─────────────────────────────────────
@app.route("/api/historial", methods=["GET"])
def historial():
    try:
        conn   = get_db()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM controles ORDER BY fecha DESC LIMIT 20")
        rows = cursor.fetchall()
        cursor.close()
        conn.close()
        for r in rows:
            r["fecha"] = str(r["fecha"])
        return jsonify(rows)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ── TEST conexión ──────────────────────────────────────────────
@app.route("/api/ping", methods=["GET"])
def ping():
    try:
        conn = get_db()
        conn.close()
        return jsonify({"status": "ok", "db": "conectado"})
    except Exception as e:
        return jsonify({"status": "error", "db": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=os.environ.get("FLASK_DEBUG") == "1")
