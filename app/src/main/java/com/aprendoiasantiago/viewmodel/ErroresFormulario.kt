package com.aprendoiasantiago.viewmodel

import androidx.annotation.StringRes
import com.aprendoiasantiago.R
import com.aprendoiasantiago.util.Validaciones

/** Mensajes de error por campo (ids de strings.xml). null = campo válido. */
object ErroresFormulario {

    @StringRes
    fun nombre(valor: String): Int? = when {
        valor.isBlank() -> R.string.error_nombre_obligatorio
        valor.trim().length < 3 -> R.string.error_nombre_min
        valor.trim().length > 30 -> R.string.error_nombre_max
        else -> null
    }

    @StringRes
    fun email(valor: String): Int? = when {
        valor.isBlank() -> R.string.error_correo_obligatorio
        !Validaciones.emailValido(valor) -> R.string.error_correo_invalido
        else -> null
    }

    @StringRes
    fun telefono(valor: String): Int? = when {
        valor.isBlank() -> R.string.error_telefono_obligatorio
        !Validaciones.telefonoValido(valor) -> R.string.error_telefono_invalido
        else -> null
    }

    @StringRes
    fun contrasena(valor: String): Int? = when {
        valor.isEmpty() -> R.string.error_contrasena_obligatoria
        !Validaciones.contrasenaValida(valor) -> R.string.error_contrasena_min
        else -> null
    }
}