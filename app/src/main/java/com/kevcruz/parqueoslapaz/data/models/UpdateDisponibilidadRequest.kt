package com.kevcruz.parqueoslapaz.data.models

data class UpdateDisponibilidadRequest(
    val cupos_libres: Int,
    val cupos_ocupados: Int,
    val usuario_id: Int
)