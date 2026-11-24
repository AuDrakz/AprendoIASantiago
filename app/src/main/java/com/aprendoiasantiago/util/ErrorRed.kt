package com.aprendoiasantiago.util

import androidx.annotation.StringRes
import com.aprendoiasantiago.R
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

enum class ErrorRed(@StringRes val mensaje: Int) {
    SIN_CONEXION(R.string.error_red_sin_conexion),
    TIMEOUT(R.string.error_red_timeout),
    CONFLICTO(R.string.error_red_conflicto),
    SERVICIO_NO_DISPONIBLE(R.string.error_red_no_disponible),
    DATOS_INVALIDOS(R.string.error_red_datos_invalidos),
    NO_ENCONTRADO(R.string.error_red_no_encontrado),
    DESCONOCIDO(R.string.error_red_desconocido)
}

fun Throwable.aErrorRed(): ErrorRed = when (this) {
    is SocketTimeoutException -> ErrorRed.TIMEOUT
    is IOException -> ErrorRed.SIN_CONEXION
    is HttpException -> when (code()) {
        400 -> ErrorRed.DATOS_INVALIDOS
        404 -> ErrorRed.NO_ENCONTRADO
        409 -> ErrorRed.CONFLICTO
        503 -> ErrorRed.SERVICIO_NO_DISPONIBLE
        else -> ErrorRed.DESCONOCIDO
    }
    else -> ErrorRed.DESCONOCIDO
}