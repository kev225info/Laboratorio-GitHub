package com.kevcruz.parqueoslapaz.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kevcruz.parqueoslapaz.data.api.RetrofitClient
import com.kevcruz.parqueoslapaz.data.models.ForgotPasswordRequest
import com.kevcruz.parqueoslapaz.databinding.ActivityForgotPasswordBinding
import kotlinx.coroutines.launch
import java.io.IOException

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnviarEnlace.setOnClickListener {
            val email = binding.etEmailForgot.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            enviarEnlaceRestablecimiento(email)
        }

        binding.tvVolverLogin.setOnClickListener {
            finish()
        }
    }

    private fun enviarEnlaceRestablecimiento(email: String) {
        binding.btnEnviarEnlace.isEnabled = false
        binding.btnEnviarEnlace.text = "Enviando..."

        lifecycleScope.launch {
            try {
                val request = ForgotPasswordRequest(email)
                val response = RetrofitClient.apiService.forgotPassword(request)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    // ⚠️ SEGURIDAD: Siempre mostrar el mismo mensaje
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        body.mensaje,
                        Toast.LENGTH_LONG
                    ).show()

                    // En desarrollo: mostrar token para facilitar pruebas
                    if (body.token_debug != null) {
                        Log.d("ForgotPassword", "Token recibido para navegación: ${body.token_debug}")
                        // Navegar directamente a ResetPasswordActivity con el token
                        val intent = Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                        intent.putExtra("TOKEN", body.token_debug)
                        startActivity(intent)
                        finish()
                    } else {
                        // Si no hay token (ej. correo no existe), solo volvemos al login
                        finish()
                    }

                } else {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Error al procesar la solicitud",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: IOException) {
                Log.e("ForgotPassword", "Error de red: ${e.message}")
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Error de conexión. Verifica tu internet.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("ForgotPassword", "Error: ${e.message}")
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Ocurrió un error inesperado",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnEnviarEnlace.isEnabled = true
                binding.btnEnviarEnlace.text = "Enviar enlace de restablecimiento"
            }
        }
    }
}