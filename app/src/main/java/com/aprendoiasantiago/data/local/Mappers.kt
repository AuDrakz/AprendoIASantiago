package com.aprendoiasantiago.data.local

import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario
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

fun PerfilEntity.toModel(
    puntosTotales: Int = 0,
    lugaresDescubiertos: Int = 0
) = PerfilUsuario(
    id = id,
    nombreUsuario = nombreUsuario,
    email = email,
    rol = rol,
    fotoRuta = fotoRuta,
    alias = alias,
    telefono = telefono,
    direccion = direccion,
    avatar = avatar,
    puntosTotales = puntosTotales,
    lugaresDescubiertos = lugaresDescubiertos
)