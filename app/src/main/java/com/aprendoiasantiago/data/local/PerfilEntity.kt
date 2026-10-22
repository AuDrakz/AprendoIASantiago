package com.aprendoiasantiago.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aprendoiasantiago.model.Rol

/**
 * Tabla "perfil": un registro por jugador. La contraseña se guarda como "sal:hash".
 */
@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombreUsuario: String,
    val alias: String? = null,
    val email: String,
    val telefono: String = "",
    val direccion: String = "",
    val avatar: Int = 0,
    val contrasenaHash: String,
    val rol: Rol = Rol.CLIENTE,
    val fotoRuta: String? = null
)