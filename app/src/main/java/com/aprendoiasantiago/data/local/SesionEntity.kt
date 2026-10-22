package com.aprendoiasantiago.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tabla "sesion": una sola fila (id = 1) con el jugador que tiene la sesión abierta. */
@Entity(tableName = "sesion")
data class SesionEntity(
    @PrimaryKey val id: Int = 1,
    val jugadorId: Int
)