package com.kevcruz.parqueoslapaz.data.models

// Para enviar al servidor
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val nombre: String, val email: String, val telefono: String, val password: String, val rol: String = "conductor")

// Respuesta del servidor
data class AuthResponse(
    val mensaje: String,
    val token: String? = null,
    val usuario: UsuarioData? = null,
    val id: Int? = null // Solo para registro
)

data class UsuarioData(val id: Int, val nombre: String, val rol: String, val parqueo_id: Int? = null)
data class FavoritoRequest(val usuario_id: Int, val parqueo_id: Int)

data class CrearAdminParqueoRequest(
    val admin_nombre: String,
    val admin_email: String,
    val admin_celular: String,
    val admin_password: String,
    val parqueo_nombre: String,
    val parqueo_direccion: String,
    val parqueo_lat: Double,
    val parqueo_lng: Double,
    val parqueo_tarifa: Double,
    val parqueo_capacidad: Int = 50
)

// Modelos para restablecimiento de contraseña
data class ForgotPasswordRequest(val email: String)

data class ForgotPasswordResponse(
    val mensaje: String,
    val token_debug: String? = null  // Solo para desarrollo
)

data class ResetPasswordRequest(
    val token: String,
    val nueva_password: String
)

data class ResetPasswordResponse(val mensaje: String)

data class UpdateDisponibilidadRequest(
    val cupos_libres: Int,
    val cupos_ocupados: Int,
    val usuario_id: Int,
    val client_updated_at: String // Nuevo campo para concurrencia
)