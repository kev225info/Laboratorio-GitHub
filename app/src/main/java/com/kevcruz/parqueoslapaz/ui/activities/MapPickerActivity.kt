package com.kevcruz.parqueoslapaz.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kevcruz.parqueoslapaz.R
import com.kevcruz.parqueoslapaz.databinding.ActivityMapPickerBinding

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var binding: ActivityMapPickerBinding
    private lateinit var mMap: GoogleMap
    private var selectedLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragmentPicker) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnConfirmarUbicacion.isEnabled = false
        binding.btnConfirmarUbicacion.setOnClickListener {
            selectedLatLng?.let { latLng ->
                val returnIntent = Intent()
                returnIntent.putExtra("LATITUDE", latLng.latitude)
                returnIntent.putExtra("LONGITUDE", latLng.longitude)
                setResult(RESULT_OK, returnIntent)
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Centrar en La Paz
        val laPaz = LatLng(-16.5000, -68.1500)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(laPaz, 14f))
        mMap.setOnMapClickListener(this)
    }

    override fun onMapClick(latLng: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
        selectedLatLng = latLng
        binding.btnConfirmarUbicacion.isEnabled = true
    }
}