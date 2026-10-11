package com.aprendoiasantiago.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ranking")
data class RankingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val puntos: Int
)