package com.aprendoiasantiago.data.remote

import com.aprendoiasantiago.data.local.PerfilEntity
import com.aprendoiasantiago.data.local.PuntoEntity
import com.aprendoiasantiago.data.local.RankingEntity
import com.aprendoiasantiago.model.PuntoInteres

/**
 * Convierte el punto del servidor en la fila de Room (la caché).
 * Conserva lo que solo existe en el dispositivo: visita, fecha, altitud y selfie.
 * Devuelve null si el servidor no envió id.
 */
fun PuntoDto.aEntity(local: PuntoEntity?): PuntoEntity? {
    val idLocal = id?.toInt() ?: return null
    return PuntoEntity(
        id = idLocal,
        titulo = titulo,
        conceptoIA = conceptoIA,
        latitud = latitud,
        longitud = longitud,
        codigoQR = codigoQR,
        puntosRecompensa = puntosRecompensa,
        urlVideo = urlVideo,
        fueVisitado = local?.fueVisitado ?: false,
        selfieRuta = local?.selfieRuta,
        fechaVisita = local?.fechaVisita,
        altitudVisita = local?.altitudVisita
    )
}

/** El id va nulo al crear (lo asigna el servidor) y con valor al actualizar. */
fun PuntoInteres.aDto() = PuntoDto(
    id = if (id == 0) null else id.toLong(),
    titulo = titulo,
    conceptoIA = conceptoIA,
    latitud = latitud,
    longitud = longitud,
    codigoQR = codigoQR,
    puntosRecompensa = puntosRecompensa,
    urlVideo = urlVideo
)

fun PerfilEntity.aJugadorDto() = JugadorDto(
    nombreUsuario = nombreUsuario,
    email = email,
    telefono = telefono.ifBlank { null },
    rol = rol.name
)

fun EntradaRankingDto.aEntity() = RankingEntity(nombre = nombre, puntos = puntos)