package com.aprendoiasantiago.model

/** Una fila del ranking global de estudiantes. */
data class EntradaRanking(
    val nombre: String,
    val puntos: Int,
    val esUsuarioActual: Boolean = false
)