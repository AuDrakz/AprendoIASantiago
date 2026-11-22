package com.aprendoiasantiago.data.remote

package com.aprendoiasantiago.data.remote

/** Punto de interés tal como lo envía y recibe ms-puntos (/api/puntos). */
data class PuntoDto(
    val id: Long? = null,
    val titulo: String,
    val conceptoIA: String,
    val latitud: Double,
    val longitud: Double,
    val codigoQR: String,
    val puntosRecompensa: Int,
    val urlVideo: String
)

/** Jugador de ms-jugadores. El rol viaja como texto: CLIENTE, GENERADOR o ADMINISTRADOR. */
data class JugadorDto(
    val id: Long? = null,
    val nombreUsuario: String,
    val email: String,
    val telefono: String? = null,
    val rol: String,
    val puntos: Int = 0
)

/** Cuerpo de POST /api/jugadores/{id}/visitas. La recompensa la decide el servidor. */
data class VisitaRequestDto(val puntoId: Long)

/** Una fila de GET /api/ranking. */
data class EntradaRankingDto(
    val nombre: String,
    val puntos: Int
)