package com.aprendoiasantiago.data.remote


import androidx.annotation.StringRes
import com.aprendoiasantiago.R
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/** Qué salió mal al hablar con el servidor, listo para mostrarse en pantalla. */
enum class TipoErrorRed(@param:StringRes val mensaje: Int) {
    SIN_CONEXION(R.string.red_sin_conexion),
    TIEMPO_AGOTADO(R.string.red_tiempo_agotado),
    SERVIDOR_NO_DISPONIBLE(R.string.red_servidor_no_disponible),
    CONFLICTO(R.string.red_conflicto),
    NO_ENCONTRADO(R.string.red_no_encontrado),
    DESCONOCIDO(R.string.red_error_generico)
}

/** Traduce una excepción de Retrofit/OkHttp a un tipo de error entendible. */
fun Throwable.aTipoErrorRed(): TipoErrorRed = when (this) {
    // SocketTimeoutException es un IOException: debe evaluarse primero
    is SocketTimeoutException -> TipoErrorRed.TIEMPO_AGOTADO
    is IOException -> TipoErrorRed.SIN_CONEXION
    is HttpException -> when (code()) {
        404 -> TipoErrorRed.NO_ENCONTRADO
        409 -> TipoErrorRed.CONFLICTO
        503 -> TipoErrorRed.SERVIDOR_NO_DISPONIBLE
        else -> TipoErrorRed.DESCONOCIDO
    }
    else -> TipoErrorRed.DESCONOCIDO
}