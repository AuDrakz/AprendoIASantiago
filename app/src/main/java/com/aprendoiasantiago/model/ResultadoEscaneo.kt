
package com.aprendoiasantiago.model

/**
 * Los posibles desenlaces al leer un código QR.
 */
sealed interface ResultadoEscaneo {

    /**
     * QR válido y punto nuevo: se sumaron los puntos.
     */
    data class Exito(
        val punto: PuntoInteres
    ) : ResultadoEscaneo

    /**
     * QR válido, pero ese lugar ya estaba visitado.
     */
    data class YaVisitado(
        val punto: PuntoInteres
    ) : ResultadoEscaneo

    /**
     * El usuario está demasiado lejos del punto.
     */
    data class FueraDeRango(
        val punto: PuntoInteres,
        val distanciaMetros: Float
    ) : ResultadoEscaneo

    /**
     * El código no corresponde a ningún punto de la aplicación.
     */
    data object Invalido : ResultadoEscaneo

    /**
     * No se pudo obtener la ubicación del usuario.
     */
    data object SinUbicacion : ResultadoEscaneo
}
