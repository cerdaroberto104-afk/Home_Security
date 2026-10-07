package com.aurahome.homesecurity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private val API_SENSORES = "http://192.168.1.85:5000/api/sensores"

    private lateinit var tvTemperatura: TextView
    private lateinit var tvHumedad: TextView
    private lateinit var tvGas: TextView
    private lateinit var tvMovimiento: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvUltimaActualizacion: TextView

    private var autoUpdateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvTemperatura         = findViewById(R.id.tvTemperatura)
        tvHumedad             = findViewById(R.id.tvHumedad)
        tvGas                 = findViewById(R.id.tvGas)
        tvMovimiento          = findViewById(R.id.tvMovimiento)
        tvEstado              = findViewById(R.id.tvEstado)
        tvUltimaActualizacion = findViewById(R.id.tvUltimaActualizacion)

        findViewById<MaterialButton>(R.id.btnActualizar).setOnClickListener { obtenerDatos() }
        findViewById<MaterialButton>(R.id.btnControl).setOnClickListener {
            startActivity(Intent(this, ControlActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            getSharedPreferences("HomeSecurity", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        obtenerDatos()

        autoUpdateJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(10000)
                obtenerDatos()
            }
        }
    }

    private fun obtenerDatos() {
        CoroutineScope(Dispatchers.IO).launch {
            val datos = fetchDatosSensores()
            withContext(Dispatchers.Main) { actualizarUI(datos) }
        }
    }

    private fun fetchDatosSensores(): Map<String, String> {
        return try {
            val conn = URL(API_SENSORES).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout    = 5000
            val respuesta = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(respuesta)
            conn.disconnect()
            mapOf(
                "temperatura" to json.getString("temperatura"),
                "humedad"     to json.getString("humedad"),
                "gas"         to json.getString("gas"),
                "movimiento"  to json.getString("movimiento"),
                "fuente"      to "AWS"
            )
        } catch (e: Exception) {
            val rand = Random()
            mapOf(
                "temperatura" to String.format("%.1f", 18.0 + rand.nextDouble() * 12),
                "humedad"     to String.format("%.0f", 40.0 + rand.nextDouble() * 40),
                "gas"         to if (rand.nextInt(10) > 8) "ALERTA" else "Normal",
                "movimiento"  to if (rand.nextInt(10) > 7) "Detectado" else "Sin mov.",
                "fuente"      to "Simulado"
            )
        }
    }

    private fun actualizarUI(datos: Map<String, String>) {
        tvTemperatura.text = "${datos["temperatura"]}°C"
        tvHumedad.text     = "${datos["humedad"]}%"

        val gas = datos["gas"] ?: "Normal"
        tvGas.text = gas
        tvGas.setTextColor(if (gas == "ALERTA") 0xFFF85149.toInt() else 0xFF3FB950.toInt())

        val mov = datos["movimiento"] ?: "Sin mov."
        tvMovimiento.text = mov
        tvMovimiento.setTextColor(if (mov == "Detectado") 0xFFD29922.toInt() else 0xFF3FB950.toInt())

        val fuente = datos["fuente"]
        tvEstado.text = if (fuente == "AWS") "✅ Sistema Activo (AWS)" else "🟡 Sistema Activo (Simulado)"

        val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        tvUltimaActualizacion.text = "Última actualización: $hora"
    }

    override fun onDestroy() {
        super.onDestroy()
        autoUpdateJob?.cancel()
    }
}