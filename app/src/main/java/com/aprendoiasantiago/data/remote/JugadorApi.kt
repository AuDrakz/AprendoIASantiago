package com.aprendoiasantiago.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface JugadorApi {
    @GET("api/jugadores") suspend fun listar(): List<JugadorDto>
    @GET("api/jugadores/{id}") suspend fun obtener(@Path("id") id: Long): JugadorDto
    @POST("api/jugadores") suspend fun crear(@Body jugador: JugadorDto): JugadorDto
    @PUT("api/jugadores/{id}") suspend fun actualizar(@Path("id") id: Long, @Body jugador: JugadorDto): JugadorDto
    @DELETE("api/jugadores/{id}") suspend fun eliminar(@Path("id") id: Long)
    @POST("api/jugadores/{id}/visitas") suspend fun registrarVisita(@Path("id") id: Long, @Body solicitud: VisitaRequestDto): JugadorDto
    @GET("api/ranking") suspend fun ranking(): List<EntradaRankingDto>
}