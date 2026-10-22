package com.aprendoiasantiago.viewmodel

import androidx.annotation.StringRes

/** Estado del formulario de registro de un nuevo jugador (Cliente). */
data class RegistroUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val direccion: String = "",
    val avatar: Int = 0,
    @StringRes val errorNombre: Int? = null,
    @StringRes val errorEmail: Int? = null,
    @StringRes val errorTelefono: Int? = null,
    @StringRes val errorContrasena: Int? = null,
    val guardando: Boolean = false
)