package com.kevcruz.parqueoslapaz.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.kevcruz.parqueoslapaz.data.api.RetrofitClient
import com.kevcruz.parqueoslapaz.data.models.CrearAdminParqueoRequest
import com.kevcruz.parqueoslapaz.databinding.ActivityGestionAdminsBinding
import kotlinx.coroutines.launch

class GestionAdminsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionAdminsBinding
    private var selectedLatLng: LatLng? = null

    // Launcher para recibir el resultado del MapPickerActivity
    private val mapPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val lat = result.data?.getDoubleExtra("LATITUDE", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("LONGITUDE", 0.0) ?: 0.0
            selectedLatLng = LatLng(lat, lng)
            binding.tvUbicacionInfo.text = "✅ Ubicación: $lat, $lng"
            binding.tvUbicacionInfo.setTextColor(getColor(android.R.color.holo_green_dark))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionAdminsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSeleccionarMapa.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            mapPickerLauncher.launch(intent)
        }

        binding.btnCrear.setOnClickListener {
            // Capturar todos los campos del formulario
            val nombre = binding.etAdminNombre.text.toString().trim()
            val celular = binding.etAdminCelular.text.toString().trim()
            val email = binding.etAdminEmail.text.toString().trim()
            val pass = binding.etAdminPass.text.toString().trim()
            val passConfirm = binding.etAdminPassConfirm.text.toString().trim()
            val nombreParqueo = binding.etParqueoNombre.text.toString().trim()
            val tarifa = binding.etParqueoTarifa.text.toString().toDoubleOrNull()
            val capacidad = binding.etParqueoCapacidad.text.toString().toIntOrNull()
            // Validaciones
            if (nombre.isEmpty() || celular.isEmpty() || email.isEmpty() ||
                pass.isEmpty() || passConfirm.isEmpty() || nombreParqueo.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != passConfirm) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tarifa == null || tarifa <= 0) {
                Toast.makeText(this, "Ingresa una tarifa válida mayor a 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (capacidad == null || capacidad <= 0) {
                Toast.makeText(this, "Ingresa una capacidad válida mayor a 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedLatLng == null) {
                Toast.makeText(this, "Debes seleccionar una ubicación en el mapa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear el request con todos los campos
            val request = CrearAdminParqueoRequest(
                admin_nombre = nombre,
                admin_email = email,
                admin_celular = celular,
                admin_password = pass,
                parqueo_nombre = nombreParqueo,
                parqueo_direccion = "Ubicación seleccionada en mapa",
                parqueo_lat = selectedLatLng!!.latitude,
                parqueo_lng = selectedLatLng!!.longitude,
                parqueo_tarifa = tarifa,
                parqueo_capacidad = capacidad  // <-- AHORA ES DINÁMICO
            )

            binding.btnCrear.isEnabled = false
            binding.btnCrear.text = "Registrando..."

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.crearAdminParqueo(request)
                    if (response.isSuccessful) {
                        Toast.makeText(this@GestionAdminsActivity, "✅ Registrado exitosamente", Toast.LENGTH_LONG).show()
                        // Limpiar formulario
                        binding.etAdminNombre.text?.clear()
                        binding.etAdminCelular.text?.clear()
                        binding.etAdminEmail.text?.clear()
                        binding.etAdminPass.text?.clear()
                        binding.etAdminPassConfirm.text?.clear()
                        binding.etParqueoNombre.text?.clear()
                        binding.etParqueoTarifa.text?.clear()
                        binding.etParqueoCapacidad.text?.clear()
                        binding.tvUbicacionInfo.text = "Ubicación: No seleccionada"
                        binding.tvUbicacionInfo.setTextColor(getColor(android.R.color.holo_red_dark))
                        selectedLatLng = null
                    } else {
                        Toast.makeText(this@GestionAdminsActivity, "❌ Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@GestionAdminsActivity, "⚠️ Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    binding.btnCrear.isEnabled = true
                    binding.btnCrear.text = "Registrar Admin y Parqueo"
                }
            }
        }

        binding.btnIrMapa.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}