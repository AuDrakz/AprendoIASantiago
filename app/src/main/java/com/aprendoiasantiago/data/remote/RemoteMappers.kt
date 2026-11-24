package com.aprendoiasantiago.data.remote

import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.model.Rol

fun PuntoDto.aModelo() = PuntoInteres(
    id = (id ?: 0L).toInt(),
    titulo = titulo,
    conceptoIA = conceptoIA,
    latitud = latitud,
    longitud = longitud,
    codigoQR = codigoQR,
    puntosRecompensa = puntosRecompensa,
    urlVideo = urlVideo
)

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

fun JugadorDto.aPerfil() = PerfilUsuario(
    id = (id ?: 0L).toInt(),
    nombreUsuario = nombreUsuario,
    email = email,
    rol = Rol.valueOf(rol),
    telefono = telefono.orEmpty(),
    puntosTotales = puntos
)

fun PerfilUsuario.aJugadorDto() = JugadorDto(
    id = if (id == 0) null else id.toLong(),
    nombreUsuario = nombreUsuario,
    email = email,
    telefono = telefono,
    rol = rol.name,
    puntos = puntosTotales
)