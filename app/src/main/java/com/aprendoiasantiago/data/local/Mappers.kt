
package com.aprendoiasantiago.data.local

import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PuntoInteres

fun PuntoEntity.toModel() = PuntoInteres(
    id = id,
    titulo = titulo,
    conceptoIA = conceptoIA,
    latitud = latitud,
    longitud = longitud,
    codigoQR = codigoQR,
    puntosRecompensa = puntosRecompensa,
    urlVideo = urlVideo,
    fueVisitado = fueVisitado,
    selfieRuta = selfieRuta,
    fechaVisita = fechaVisita,
    altitudVisita = altitudVisita
)

fun PuntoInteres.toEntity() = PuntoEntity(
    id = id,
    titulo = titulo,
    conceptoIA = conceptoIA,
    latitud = latitud,
    longitud = longitud,
    codigoQR = codigoQR,
    puntosRecompensa = puntosRecompensa,
    urlVideo = urlVideo,
    fueVisitado = fueVisitado,
    selfieRuta = selfieRuta,
    fechaVisita = fechaVisita,
    altitudVisita = altitudVisita
)

fun RankingEntity.toModel() = EntradaRanking(
    nombre = nombre,
    puntos = puntos
)
