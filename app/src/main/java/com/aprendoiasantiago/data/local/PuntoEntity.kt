package com.aprendoiasantiago.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla "puntos": un registro por punto de interés.
 * Es distinta de la clase PuntoInteres del paquete model para que la
 * base de datos pueda cambiar sin tocar la UI (y al revés).
 */
@Entity(tableName = "puntos")
data class PuntoEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val conceptoIA: String,
    val latitud: Double,
    val longitud: Double,
    val codigoQR: String,
    val puntosRecompensa: Int,
    val urlVideo: String,
    val fueVisitado: Boolean = false
)