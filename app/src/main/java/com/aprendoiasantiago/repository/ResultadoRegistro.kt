package com.aprendoiasantiago.repository

/** Resultado de crear una cuenta nueva. */
enum class ResultadoRegistro {
    EXITO,
    CORREO_DUPLICADO,
    NOMBRE_DUPLICADO
}