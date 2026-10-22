package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.model.Rol

/** Estado de la sesión: decide si se muestra el acceso o la app. */
sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class ConSesion(val jugadorId: Int, val rol: Rol) : EstadoSesion
}