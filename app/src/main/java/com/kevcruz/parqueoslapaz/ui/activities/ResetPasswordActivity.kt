package com.kevcruz.parqueoslapaz.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kevcruz.parqueoslapaz.data.api.RetrofitClient
import com.kevcruz.parqueoslapaz.data.models.ResetPasswordRequest
import com.kevcruz.parqueoslapaz.databinding.ActivityResetPasswordBinding
import kotlinx.coroutines.launch
import java.io.IOException

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private var tokenRecibido: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibir token desde ForgotPasswordActivity (solo en desarrollo)
        tokenRecibido = intent.getStringExtra("TOKEN") ?: ""
        if (tokenRecibido.isNotEmpty()) {
            binding.etToken.setText(tokenRecibido)
        }

        binding.btnGuardarPassword.setOnClickListener {
            val token = binding.etToken.text.toString().trim()
            val nuevaPassword = binding.etNuevaPassword.text.toString().trim()
            val confirmarPassword = binding.etConfirmarPassword.text.toString().trim()

            // Validaciones
            if (token.isEmpty()) {
                Toast.makeText(this, "Ingresa el token", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevaPassword.isEmpty() || confirmarPassword.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevaPassword.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevaPassword != confirmarPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarNuevaPassword(token, nuevaPassword)
        }
    }

    private fun guardarNuevaPassword(token: String, nuevaPassword: String) {
        binding.btnGuardarPassword.isEnabled = false
        binding.btnGuardarPassword.text = "Guardando..."

        lifecycleScope.launch {
            try {
                val request = ResetPasswordRequest(token, nuevaPassword)
                val response = RetrofitClient.apiService.resetPassword(request)

                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        response.body()!!.mensaje,
                        Toast.LENGTH_LONG
                    ).show()

                    // Volver al login para que inicie sesión con la nueva contraseña
                    val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error del servidor"
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Error: $errorMsg",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: IOException) {
                Log.e("ResetPassword", "Error de red: ${e.message}")
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "Error de conexión",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("ResetPassword", "Error: ${e.message}")
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "Ocurrió un error inesperado",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnGuardarPassword.isEnabled = true
                binding.btnGuardarPassword.text = "Guardar nueva contraseña"
            }
        }
    }
}