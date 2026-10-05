package com.aprendoiasantiago.data.local

import com.aprendoiasantiago.model.PuntoInteres

// Convierten entre la tabla (Entity) y el modelo que usa la UI.

fun PuntoEntity.toModel() = PuntoInteres(
    id = id,
    titulo = titulo,
    conceptoIA = conceptoIA,
    latitud = latitud,
    longitud = longitud,
    codigoQR = codigoQR,
    puntosRecompensa = puntosRecompensa,
    urlVideo = urlVideo,
    fueVisitado = fueVisitado
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
    fueVisitado = fueVisitado
)