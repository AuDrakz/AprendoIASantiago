package com.aprendoiasantiago.data.local

import androidx.room.Entity

@Entity(tableName = "visitas", primaryKeys = ["jugadorId", "puntoId"])
data class VisitaEntity(
    val jugadorId: Int,
    val puntoId: Int,
    val fecha: String,
    val altitud: Double? = null,
    val selfieRuta: String? = null
)