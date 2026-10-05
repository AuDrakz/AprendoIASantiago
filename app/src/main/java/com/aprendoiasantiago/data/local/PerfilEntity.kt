package com.aprendoiasantiago.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla "perfil". Solo guarda los datos fijos del usuario.
 * Los puntos totales y lugares descubiertos se calculan desde la
 * tabla "puntos", así nunca quedan desincronizados.
 */
@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey val id: Int,
    val nombreUsuario: String,
    val email: String
)