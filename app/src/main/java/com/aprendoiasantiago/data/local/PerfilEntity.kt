package com.aprendoiasantiago.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aprendoiasantiago.model.Rol

/**
 * Tabla "perfil". Guarda los datos fijos del usuario, su rol y la ruta de su foto.
 * Los puntos y lugares se calculan desde la tabla "puntos".
 */
@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey val id: Int,
    val nombreUsuario: String,
    val email: String,
    val rol: Rol = Rol.CLIENTE,
    val fotoRuta: String? = null
)