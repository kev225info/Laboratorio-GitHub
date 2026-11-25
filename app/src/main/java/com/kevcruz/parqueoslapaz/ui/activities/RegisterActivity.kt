package com.kevcruz.parqueoslapaz.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kevcruz.parqueoslapaz.data.api.RetrofitClient
import com.kevcruz.parqueoslapaz.data.models.RegisterRequest
import com.kevcruz.parqueoslapaz.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflar el layout con ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // 1. Navegar a Login
        binding.tvGoToLogin.setOnClickListener {
            finish()
        }

        // 2. Botón Registrarse (CONEXIÓN REAL A LA API EN RENDER.COM)
        binding.btnRegister.setOnClickListener {
            val name = binding.etNameRegister.text.toString().trim()
            val email = binding.etEmailRegister.text.toString().trim()
            val phone = binding.etPhoneRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                performRegistration(name, email, phone, password)
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performRegistration(name: String, email: String, phone: String, password: String) {
        // Deshabilitar botón para evitar múltiples clics
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Registrando..."

        lifecycleScope.launch {
            try {
                // Crear el objeto de petición (el rol por defecto será "conductor")
                val request = RegisterRequest(name, email, phone, password, "conductor")

                // Llamar a la API de Registro
                val response = RetrofitClient.apiService.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    Toast.makeText(this@RegisterActivity, authResponse.mensaje, Toast.LENGTH_LONG).show()

                    // Volver a la pantalla de Login para que inicie sesión con sus nuevas credenciales
                    finish()
                } else {
                    // Manejar errores del servidor (ej: correo ya registrado)
                    val errorMsg = response.errorBody()?.string() ?: "Error en el servidor"
                    Toast.makeText(this@RegisterActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("RegisterError", "Error de red: ${e.message}")
                Toast.makeText(this@RegisterActivity, "Error de conexión. Verifica que el servidor esté corriendo.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("RegisterError", "Error inesperado: ${e.message}")
                Toast.makeText(this@RegisterActivity, "Ocurrió un error inesperado", Toast.LENGTH_LONG).show()
            } finally {
                // Restaurar el botón
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Registrarse"
            }
        }
    }
}