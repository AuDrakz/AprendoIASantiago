package com.aprendoiasantiago.model

/**
 * Representa al usuario de la app y su progreso gamificado.
 */
data class PerfilUsuario(
    val id: Int,
    val nombreUsuario: String,
    val email: String,
    val puntosTotales: Int = 0,
    val lugaresDescubiertos: Int = 0
)