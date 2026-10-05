package com.aprendoiasantiago.model

/**
 * Representa un punto de interés inmutable georreferenciado.
 */
data class PuntoInteres(
    val id: Int,
    val titulo: String,
    val conceptoIA: String,
    val latitud: Double,
    val longitud: Double,
    val codigoQR: String,
    val puntosRecompensa: Int,
    val urlVideo: String,
    val fueVisitado: Boolean = false
)