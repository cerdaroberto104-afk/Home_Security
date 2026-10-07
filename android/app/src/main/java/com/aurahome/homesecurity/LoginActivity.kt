package com.aurahome.homesecurity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {

    private val API_URL = "http://192.168.1.85:5000/api/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsuario = findViewById<TextInputEditText>(R.id.etUsuario)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin   = findViewById<MaterialButton>(R.id.btnLogin)
        val tvError    = findViewById<TextView>(R.id.tvError)

        btnLogin.setOnClickListener {
            val usuario  = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (usuario.isEmpty() || password.isEmpty()) {
                tvError.text = "⚠️ Completa todos los campos"
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "Verificando..."
            tvError.text = ""

            CoroutineScope(Dispatchers.IO).launch {
                val resultado = loginConAPI(usuario, password)
                withContext(Dispatchers.Main) {
                    btnLogin.isEnabled = true
                    btnLogin.text = "INGRESAR"
                    if (resultado) {
                        val prefs = getSharedPreferences("HomeSecurity", MODE_PRIVATE)
                        prefs.edit().putString("usuario", usuario).putBoolean("logueado", true).apply()
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        tvError.text = "❌ Usuario o contraseña incorrectos"
                    }
                }
            }
        }
    }

    private fun loginConAPI(usuario: String, password: String): Boolean {
        return try {
            val url  = URL(API_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 5000
            conn.readTimeout    = 5000

            val body = JSONObject().apply {
                put("usuario", usuario)
                put("password", password)
            }.toString()

            conn.outputStream.use { it.write(body.toByteArray()) }
            val respuesta = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(respuesta)
            conn.disconnect()
            json.getBoolean("success")
        } catch (e: Exception) {
            false
        }
    }
}