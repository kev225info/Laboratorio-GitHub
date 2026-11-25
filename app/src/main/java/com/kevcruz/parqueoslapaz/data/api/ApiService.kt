package com.kevcruz.parqueoslapaz.data.api

import com.kevcruz.parqueoslapaz.data.models.AuthResponse
import com.kevcruz.parqueoslapaz.data.models.BusquedaResponse
import com.kevcruz.parqueoslapaz.data.models.CrearAdminParqueoRequest
import com.kevcruz.parqueoslapaz.data.models.FavoritoRequest
import com.kevcruz.parqueoslapaz.data.models.ForgotPasswordRequest
import com.kevcruz.parqueoslapaz.data.models.ForgotPasswordResponse
import com.kevcruz.parqueoslapaz.data.models.LoginRequest
import com.kevcruz.parqueoslapaz.data.models.Parqueo
import com.kevcruz.parqueoslapaz.data.models.ParqueoCompleto
import com.kevcruz.parqueoslapaz.data.models.RegisterRequest
import com.kevcruz.parqueoslapaz.data.models.ResetPasswordRequest
import com.kevcruz.parqueoslapaz.data.models.ResetPasswordResponse
import com.kevcruz.parqueoslapaz.data.models.UpdateDisponibilidadRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/admin/crear-admin-parqueo")
    suspend fun crearAdminParqueo(@Body request: CrearAdminParqueoRequest): Response<Map<String, String>>

    @GET("api/parqueos/mi-parqueo") // Nuevo endpoint para que el admin_parqueo solo vea el suyo
    suspend fun getMiParqueo(@Query("usuario_id") usuarioId: Int): Response<Parqueo>
    @GET("api/parqueos")
    suspend fun getParqueos(): Response<List<Parqueo>>

    @GET("api/parqueos/favoritos/{usuarioId}")
    suspend fun getFavoritos(@Path("usuarioId") usuarioId: Int): Response<List<Int>>

    @POST("api/parqueos/favoritos")
    suspend fun toggleFavorito(@Body request: FavoritoRequest): Response<Map<String, String>>
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>
    @GET("api/parqueos/parqueo-completo")
    suspend fun getParqueoCompleto(@Query("usuario_id") usuarioId: Int): Response<ParqueoCompleto>
    @GET("api/parqueos/buscar")
    suspend fun buscarParqueos(
        @Query("query") query: String,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radio_km") radioKm: Double? = null
    ): Response<BusquedaResponse>
    @PATCH("api/parqueos/{parqueoId}/disponibilidad")
    suspend fun updateDisponibilidad(
        @Path("parqueoId") parqueoId: Int,
        @Body request: UpdateDisponibilidadRequest
    ): Response<Map<String, String>>
}