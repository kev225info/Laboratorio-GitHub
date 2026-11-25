package com.kevcruz.parqueoslapaz.data.models

data class Parqueo(
    val id: Int,
    val nombre: String,
    val direccion: String,
    val latitud: String,          // <-- CAMBIADO A STRING (MySQL lo envía entre comillas)
    val longitud: String,         // <-- CAMBIADO A STRING
    val tarifa_por_hora: String,  // <-- CAMBIADO A STRING
    val horario_apertura: String,
    val horario_cierre: String,
    val tipo_vehiculo: String,
    val imagen_url: String?,
    val activo: Int,              // <-- CAMBIADO A INT (MySQL envía 1 o 0, no true/false)
    val cupos_libres: Int,
    val cupos_ocupados: Int,
    val capacidad_total: Int,
    val telefono_contacto: String? = null
)
data class ParqueoCompleto(
    val parqueo_id: Int,
    val parqueo_nombre: String,
    val direccion: String,
    val latitud: String,
    val longitud: String,
    val capacidad_total: Int,
    val tarifa_por_hora: String,
    val horario_apertura: String,
    val horario_cierre: String,
    val tipo_vehiculo: String,
    val descripcion: String?,
    val imagen_url: String?,
    val activo: Int,
    val admin_id: Int,
    val admin_nombre: String,
    val admin_email: String,
    val admin_telefono: String,
    val rol: String,
    val cupos_libres: Int,
    val cupos_ocupados: Int,
    val updated_at: String
)

// Modelos para búsqueda inteligente
data class BusquedaResponse(
    val resultados: List<Parqueo>,
    val sugerencias: List<String>,
    val mensaje: String? = null,
    val total: Int
)