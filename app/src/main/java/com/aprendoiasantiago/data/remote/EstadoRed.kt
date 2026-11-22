package com.aprendoiasantiago.data.remote


/** Estado de la última operación contra el backend; lo observa la UI para avisar al usuario. */
sealed interface EstadoRed {
    data object Inactivo : EstadoRed
    data object Cargando : EstadoRed
    data object Exito : EstadoRed
    data class Error(val tipo: TipoErrorRed) : EstadoRed
}