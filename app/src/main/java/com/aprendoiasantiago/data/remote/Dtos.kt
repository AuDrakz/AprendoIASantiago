package com.aprendoiasantiago.data.remote

data class PuntoDto(
    val id: Long? = null,
    val titulo: String,
    val conceptoIA: String,
    val latitud: Double,
    val longitud: Double,
    val codigoQR: String,
    val puntosRecompensa: Int,
    val urlVideo: String
)

data class JugadorDto(
    val id: Long? = null,
    val nombreUsuario: String,
    val email: String,
    val telefono: String? = "",
    val rol: String,
    val puntos: Int = 0
)

data class VisitaRequestDto(val puntoId: Long)

data class EntradaRankingDto(val nombre: String, val puntos: Int)