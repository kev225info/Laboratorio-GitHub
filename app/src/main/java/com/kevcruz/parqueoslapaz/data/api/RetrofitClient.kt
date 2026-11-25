package com.kevcruz.parqueoslapaz.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // IMPORTANTE:
    // Si usas el EMULADOR de Android Studio, usa "10.0.2.2" (es el localhost de tu PC).
    // Si usas un DISPOSITIVO FÍSICO, usa la dirección IP de tu PC en la red Wi-Fi (ej: "192.168.0.4").
    private const val BASE_URL = "https://parqueos-lapaz-api.onrender.com/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}