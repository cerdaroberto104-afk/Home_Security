package com.aurahome.homesecurity

import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ControlActivity : AppCompatActivity() {

    private val API_CONTROL = "http://192.168.1.85:5000/api/control"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        val switchRele      = findViewById<Switch>(R.id.switchRele)
        val switchBuzzer    = findViewById<Switch>(R.id.switchBuzzer)
        val tvEstadoRele    = findViewById<TextView>(R.id.tvEstadoRele)
        val tvEstadoBuzzer  = findViewById<TextView>(R.id.tvEstadoBuzzer)
        val tvEstadoServo   = findViewById<TextView>(R.id.tvEstadoServo)

        switchRele.setOnCheckedChangeListener { _, isChecked ->
            val estado = if (isChecked) "ON" else "OFF"
            tvEstadoRele.text = "Estado: $estado"
            enviarComando("rele", estado)
        }

        switchBuzzer.setOnCheckedChangeListener { _, isChecked ->
            val estado = if (isChecked) "ON" else "OFF"
            tvEstadoBuzzer.text = "Estado: $estado"
            enviarComando("buzzer", estado)
        }

        findViewById<MaterialButton>(R.id.btnAbrir).setOnClickListener {
            tvEstadoServo.text = "Estado: ABIERTO"
            enviarComando("servo", "ABRIR")
        }

        findViewById<MaterialButton>(R.id.btnCerrar).setOnClickListener {
            tvEstadoServo.text = "Estado: CERRADO"
            enviarComando("servo", "CERRAR")
        }

        findViewById<MaterialButton>(R.id.btnVolver).setOnClickListener {
            finish()
        }
    }

    private fun enviarComando(dispositivo: String, estado: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL(API_CONTROL).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 5000
                val body = JSONObject().apply {
                    put("dispositivo", dispositivo)
                    put("estado", estado)
                }.toString()
                conn.outputStream.use { it.write(body.toByteArray()) }
                conn.inputStream.bufferedReader().readText()
                conn.disconnect()
            } catch (e: Exception) {
                // Sin conexión: el switch igual cambia visualmente
            }
        }
    }
}