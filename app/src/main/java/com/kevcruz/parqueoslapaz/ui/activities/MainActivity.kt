package com.kevcruz.parqueoslapaz.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kevcruz.parqueoslapaz.R
import com.kevcruz.parqueoslapaz.data.api.RetrofitClient
import com.kevcruz.parqueoslapaz.data.models.FavoritoRequest
import com.kevcruz.parqueoslapaz.data.models.Parqueo
import com.kevcruz.parqueoslapaz.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // 1. Declaración correcta de ViewBinding
    private lateinit var binding: ActivityMainBinding

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var usuarioId: Int = -1
    private var favoritosList: List<Int> = emptyList()

    private val LA_PAZ_LAT_LNG = LatLng(-16.5000, -68.1500)
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // 2. Mapa para rastrear los marcadores por su ID (Solución al error de 'markers')
    private val markerMap = mutableMapOf<Int, Marker>()

    // 3. Launcher para recibir resultados de la búsqueda
    private val searchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val parqueoId = result.data?.getIntExtra("PARQUEO_ID", -1) ?: -1
            val lat = result.data?.getDoubleExtra("PARQUEO_LAT", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("PARQUEO_LNG", 0.0) ?: 0.0

            if (parqueoId != -1) {
                // Centrar el mapa en el parqueo seleccionado
                val posicion = LatLng(lat, lng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicion, 16f))

                // Buscar el marcador en nuestro diccionario (markerMap)
                val markerEncontrado = markerMap[parqueoId]

                if (markerEncontrado != null) {
                    markerEncontrado.showInfoWindow()
                    val parqueoSeleccionado = markerEncontrado.tag as? Parqueo
                    if (parqueoSeleccionado != null) {
                        mostrarDialogoParqueo(parqueoSeleccionado)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding correctamente
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        usuarioId = prefs.getInt("usuario_id", -1)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar botón de búsqueda (si lo agregaste al XML)
        binding.fabBuscar.setOnClickListener {
            val intent = Intent(this, BusquedaActivity::class.java)
            searchLauncher.launch(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LA_PAZ_LAT_LNG, 13f))

        checkLocationPermission()
        loadParqueosFromAPI()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableMyLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
    }

    private fun loadParqueosFromAPI() {
        lifecycleScope.launch {
            try {
                val responseParqueos = RetrofitClient.apiService.getParqueos()
                if (responseParqueos.isSuccessful && responseParqueos.body() != null) {
                    val parqueos = responseParqueos.body()!!

                    if (usuarioId != -1) {
                        val responseFavs = RetrofitClient.apiService.getFavoritos(usuarioId)
                        if (responseFavs.isSuccessful) favoritosList = responseFavs.body() ?: emptyList()
                    }

                    dibujarMarcadores(parqueos)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error cargando datos: ${e.message}")
            }
        }
    }

    private fun dibujarMarcadores(parqueos: List<Parqueo>) {
        mMap.clear()
        markerMap.clear() // Limpiar también nuestro diccionario de marcadores
        mMap.setOnMarkerClickListener(this)

        for (parqueo in parqueos) {
            val lat = parqueo.latitud.toDoubleOrNull() ?: -16.5000
            val lng = parqueo.longitud.toDoubleOrNull() ?: -68.1500
            val posicion = LatLng(lat, lng)

            val markerColor = if (parqueo.cupos_libres > 0) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED

            val marker = mMap.addMarker(
                MarkerOptions().position(posicion).title(parqueo.nombre)
                    .snippet("Libres: ${parqueo.cupos_libres} | Tarifa: Bs ${parqueo.tarifa_por_hora}")
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            )

            if (marker != null) {
                marker.tag = parqueo
                // 4. Guardar el marcador en el diccionario usando su ID como clave
                markerMap[parqueo.id] = marker
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val parqueo = marker.tag as? Parqueo
        if (parqueo != null) {
            mostrarDialogoParqueo(parqueo)
            return true
        }
        return false
    }

    private fun mostrarDialogoParqueo(parqueo: Parqueo) {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val rolUsuario = prefs.getString("usuario_rol", "conductor")
        val esInvitado = (rolUsuario == "invitado")
        val telefono = parqueo.telefono_contacto ?: "No registrado"

        val builder = AlertDialog.Builder(this)
            .setTitle(parqueo.nombre)
            .setMessage(
                "📍 Dirección: ${parqueo.direccion}\n" +
                        "🕒 Horario: ${parqueo.horario_apertura} - ${parqueo.horario_cierre}\n" +
                        "🚗 Disponibilidad: ${parqueo.cupos_libres} de ${parqueo.capacidad_total}\n" +
                        "💰 Tarifa: Bs ${parqueo.tarifa_por_hora}/hora\n" +
                        "📞 Teléfono: $telefono" +
                        if (esInvitado) "\n\n💡 Inicia sesión para guardar en favoritos." else ""
            )
            .setPositiveButton("🗺️ Cómo llegar") { _, _ -> abrirGoogleMaps(parqueo) }

        if (!esInvitado) {
            val esFavorito = favoritosList.contains(parqueo.id)
            val textoFavorito = if (esFavorito) "Quitar de Favoritos" else "Guardar en Favoritos"
            builder.setNeutralButton(textoFavorito) { _, _ -> toggleFavorito(parqueo.id) }
        }

        builder.setNegativeButton("Cerrar", null).create().show()
    }

    private fun abrirGoogleMaps(parqueo: Parqueo) {
        val gmmIntentUri = Uri.parse("google.navigation:q=${parqueo.latitud},${parqueo.longitud}&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(this, "Google Maps no está instalado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFavorito(parqueoId: Int) {
        if (usuarioId <= 0) {
            Toast.makeText(this, "Debes iniciar sesión para usar favoritos", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = FavoritoRequest(usuarioId, parqueoId)
                val response = RetrofitClient.apiService.toggleFavorito(request)
                if (response.isSuccessful) {
                    val mensaje = response.body()?.get("mensaje") ?: "Actualizado"
                    Toast.makeText(this@MainActivity, mensaje, Toast.LENGTH_SHORT).show()
                    loadParqueosFromAPI()
                }
            } catch (e: IOException) {
                Toast.makeText(this@MainActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }
}