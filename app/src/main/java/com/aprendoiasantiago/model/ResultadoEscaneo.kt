package com.aprendoiasantiago.model

/** Los tres desenlaces posibles al leer un código QR. */
sealed interface ResultadoEscaneo {
    /** QR válido y punto nuevo: se sumaron los puntos. */
    data class Exito(val punto: PuntoInteres) : ResultadoEscaneo

    /** QR válido, pero ese lugar ya estaba visitado. */
    data class YaVisitado(val punto: PuntoInteres) : ResultadoEscaneo

    /** El código no corresponde a ningún punto de la app. */
    data object Invalido : ResultadoEscaneo
}