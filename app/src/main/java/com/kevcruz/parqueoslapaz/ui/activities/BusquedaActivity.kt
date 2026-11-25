package com.kevcruz.parqueoslapaz.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import com.kevcruz.parqueoslapaz.data.api.RetrofitClient
import com.kevcruz.parqueoslapaz.data.models.Parqueo
import com.kevcruz.parqueoslapaz.databinding.ActivityBusquedaBinding
import com.kevcruz.parqueoslapaz.ui.adapters.BusquedaAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BusquedaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBusquedaBinding
    private lateinit var adapter: BusquedaAdapter
    private var searchJob: Job? = null
    private var ubicacionActual: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusquedaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener ubicación actual de SharedPreferences si está disponible
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("ultima_lat", 0f)
        val lng = prefs.getFloat("ultima_lng", 0f)
        if (lat != 0f && lng != 0f) {
            ubicacionActual = LatLng(lat.toDouble(), lng.toDouble())
        }

        setupRecyclerView()
        setupSearchBar()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbarBusqueda.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = BusquedaAdapter { parqueo ->
            // Al hacer clic en un resultado, volver al mapa con el parqueo seleccionado
            val intent = Intent()
            intent.putExtra("PARQUEO_ID", parqueo.id)
            intent.putExtra("PARQUEO_LAT", parqueo.latitud.toDoubleOrNull() ?: 0.0)
            intent.putExtra("PARQUEO_LNG", parqueo.longitud.toDoubleOrNull() ?: 0.0)
            setResult(RESULT_OK, intent)
            finish()
        }

        binding.recyclerResultados.apply {
            layoutManager = LinearLayoutManager(this@BusquedaActivity)
            adapter = this@BusquedaActivity.adapter
        }
    }

    private fun setupSearchBar() {
        binding.etBusqueda.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etBusqueda.text.toString().trim()
                if (query.isNotEmpty()) {
                    buscarParqueos(query)
                }
                true
            } else {
                false
            }
        }

        // Búsqueda en tiempo real con debounce de 300ms
        binding.etBusqueda.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                searchJob?.cancel() // Cancelar búsqueda anterior

                val query = s.toString().trim()

                if (query.length < 2) {
                    binding.layoutSinResultados.visibility = View.GONE
                    binding.recyclerResultados.visibility = View.GONE
                    binding.progressBusqueda.visibility = View.GONE
                    return
                }

                // Debounce: esperar 300ms antes de buscar
                searchJob = lifecycleScope.launch {
                    delay(300) // ⏱️ RETARDO DE 300ms
                    buscarParqueos(query)
                }
            }
        })
    }

    private fun buscarParqueos(query: String) {
        binding.progressBusqueda.visibility = View.VISIBLE
        binding.recyclerResultados.visibility = View.GONE
        binding.layoutSinResultados.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.buscarParqueos(
                    query = query,
                    lat = ubicacionActual?.latitude,
                    lng = ubicacionActual?.longitude,
                    radioKm = 5.0 // Radio de 5km para sugerencias
                )

                if (response.isSuccessful && response.body() != null) {
                    val busquedaResponse = response.body()!!

                    if (busquedaResponse.resultados.isNotEmpty()) {
                        // Mostrar resultados
                        adapter.setParqueos(busquedaResponse.resultados)
                        binding.recyclerResultados.visibility = View.VISIBLE
                        binding.layoutSinResultados.visibility = View.GONE
                    } else {
                        // Mostrar sugerencias
                        binding.recyclerResultados.visibility = View.GONE
                        binding.layoutSinResultados.visibility = View.VISIBLE

                        binding.tvSinResultados.text = busquedaResponse.mensaje
                            ?: "No encontramos parqueos para \"$query\""

                        // Crear chips con las sugerencias
                        binding.chipGroupZonas.removeAllViews()
                        busquedaResponse.sugerencias.forEach { zona ->
                            val chip = Chip(this@BusquedaActivity).apply {
                                text = zona
                                isClickable = true
                                isCheckable = false
                                setOnClickListener {
                                    binding.etBusqueda.setText(zona)
                                    buscarParqueos(zona)
                                }
                            }
                            binding.chipGroupZonas.addView(chip)
                        }
                    }
                } else {
                    Toast.makeText(this@BusquedaActivity, "Error en la búsqueda", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BusquedaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBusqueda.visibility = View.GONE
            }
        }
    }
}