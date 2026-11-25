package com.kevcruz.parqueoslapaz.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kevcruz.parqueoslapaz.data.api.RetrofitClient
import com.kevcruz.parqueoslapaz.data.models.LoginRequest
import com.kevcruz.parqueoslapaz.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // NUEVO: Navegar a ForgotPasswordActivity
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailLogin.text.toString().trim()
            val password = binding.etPasswordLogin.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                performLogin(email, password)
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
        // NUEVO: Lógica para el modo invitado
        binding.btnGuest.setOnClickListener {
            val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("usuario_id", 0).apply()
            prefs.edit().putString("usuario_rol", "invitado").apply()
            prefs.edit().putInt("usuario_parqueo_id", 0).apply()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun performLogin(email: String, password: String) {
        binding.btnLogin.isEnabled = false // Evitar múltiples clics
        binding.btnLogin.text = "Cargando..."

        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = RetrofitClient.apiService.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putInt("usuario_id", authResponse.usuario!!.id).apply()
                    prefs.edit().putString("usuario_rol", authResponse.usuario.rol).apply()
                    prefs.edit().putInt("usuario_parqueo_id", authResponse.usuario.parqueo_id ?: 0).apply()

                    // ⚠️ CORRECCIÓN AQUÍ: Usar this@LoginActivity en lugar de this
                    when (authResponse.usuario.rol) {
                        "admin" -> startActivity(Intent(this@LoginActivity, GestionAdminsActivity::class.java))
                        "admin_parqueo" -> startActivity(Intent(this@LoginActivity, PanelAdminParqueoActivity::class.java))
                        else -> startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error en el servidor"
                    Toast.makeText(this@LoginActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("LoginError", "Error de red: ${e.message}")
                Toast.makeText(this@LoginActivity, "Error de conexión. Verifica que el servidor esté corriendo.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("LoginError", "Error inesperado: ${e.message}")
                Toast.makeText(this@LoginActivity, "Ocurrió un error inesperado", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Iniciar Sesión"
            }
        }
    }
}