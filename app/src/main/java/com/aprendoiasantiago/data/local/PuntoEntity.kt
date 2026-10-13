package com.aprendoiasantiago.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puntos_interes")
data class PuntoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val esVisitado: Boolean = false,
    val altitudVisita: Double? = null,
    val fechaVisita: Long? = null,
    val urlVideo: String? = null
)