package com.aprendoiasantiago.model

/**
 * Representa a un jugador de la app y su progreso gamificado.
 */
data class PerfilUsuario(
    val id: Int,
    val nombreUsuario: String,
    val email: String,
    val rol: Rol = Rol.CLIENTE,
    val fotoRuta: String? = null,
    val alias: String? = null,
    val telefono: String = "",
    val direccion: String = "",
    val avatar: Int = 0,
    val puntosTotales: Int = 0,
    val lugaresDescubiertos: Int = 0
)