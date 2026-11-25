package com.kevcruz.parqueoslapaz.ui.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kevcruz.parqueoslapaz.data.api.RetrofitClient
import com.kevcruz.parqueoslapaz.data.models.UpdateDisponibilidadRequest
import com.kevcruz.parqueoslapaz.databinding.ActivityPanelAdminParqueoBinding
import kotlinx.coroutines.launch

class PanelAdminParqueoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPanelAdminParqueoBinding
    private var usuarioId: Int = -1
    private var parqueoId: Int = -1
    private var parqueoUpdatedAt: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelAdminParqueoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        usuarioId = prefs.getInt("usuario_id", -1)

        if (usuarioId == -1) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.btnActualizar.isEnabled = false // se habilita al cargar datos
        cargarInformacionCompleta()
        configurarBotones()
    }

    private fun cargarInformacionCompleta() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getParqueoCompleto(usuarioId)
                val info = response.body()

                if (response.isSuccessful && info != null) {
                    parqueoId = info.parqueo_id
                    parqueoUpdatedAt = info.updated_at // ✅ Se guarda la versión aquí

                    // Información del Parqueo (solo lectura)
                    binding.tvParqueoNombre.text = "Nombre: ${info.parqueo_nombre}"
                    binding.tvParqueoDireccion.text = "Dirección: ${info.direccion}"
                    binding.tvParqueoTarifa.text = "Tarifa: Bs ${info.tarifa_por_hora}/hora"
                    binding.tvParqueoHorario.text = "Horario: ${info.horario_apertura} - ${info.horario_cierre}"
                    binding.tvParqueoTipo.text = "Tipo de vehículo: ${info.tipo_vehiculo}"

                    // Información del administrador (solo lectura)
                    binding.tvAdminNombre.text = "Nombre: ${info.admin_nombre}"
                    binding.tvAdminEmail.text = "Email: ${info.admin_email}"
                    binding.tvAdminTelefono.text = "Teléfono: ${info.admin_telefono}"

                    // Campos editables
                    binding.etCapacidadTotal.setText(info.capacidad_total.toString())
                    binding.etCuposLibres.setText(info.cupos_libres.toString())

                    val ocupados = info.capacidad_total - info.cupos_libres
                    binding.tvCuposOcupados.text = "Cupos Ocupados: $ocupados (calculado automáticamente)"

                    binding.btnActualizar.isEnabled = true
                } else {
                    Toast.makeText(this@PanelAdminParqueoActivity, "No tienes un parqueo asignado", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PanelAdminParqueoActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun configurarBotones() {
        binding.btnActualizar.setOnClickListener {
            if (parqueoId == -1) {
                Toast.makeText(this, "Parqueo no cargado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val capacidadTotal = binding.etCapacidadTotal.text.toString().toIntOrNull()
            val cuposLibres = binding.etCuposLibres.text.toString().toIntOrNull()

            if (capacidadTotal == null || cuposLibres == null ||
                capacidadTotal <= 0 || cuposLibres < 0 || cuposLibres > capacidadTotal
            ) {
                Toast.makeText(this, "Datos de capacidad inválidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cuposOcupados = capacidadTotal - cuposLibres

            binding.btnActualizar.isEnabled = false
            binding.btnActualizar.text = "Guardando..."

            lifecycleScope.launch {
                try {
                    val request = UpdateDisponibilidadRequest(
                        cuposLibres = cuposLibres,
                        cuposOcupados = cuposOcupados,
                        usuario_id = usuarioId,
                        client_updated_at = parqueoUpdatedAt
                    )
                    val response = RetrofitClient.apiService.updateDisponibilidad(parqueoId, request)

                    when {
                        response.isSuccessful -> {
                            Toast.makeText(this@PanelAdminParqueoActivity, "✅ Actualizado correctamente", Toast.LENGTH_LONG).show()
                            cargarInformacionCompleta() // refresca updated_at
                        }
                        response.code() == 409 -> {
                            Toast.makeText(this@PanelAdminParqueoActivity, "⚠️ Otro administrador modificó este parqueo. Recargando datos...", Toast.LENGTH_LONG).show()
                            cargarInformacionCompleta()
                        }
                        else -> {
                            Toast.makeText(this@PanelAdminParqueoActivity, "❌ Error: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@PanelAdminParqueoActivity, "⚠️ Error de red", Toast.LENGTH_LONG).show()
                } finally {
                    binding.btnActualizar.isEnabled = true
                    binding.btnActualizar.text = "💾 Actualizar Disponibilidad"
                }
            }
        }
    }
}