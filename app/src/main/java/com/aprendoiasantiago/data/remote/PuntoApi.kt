package com.aprendoiasantiago.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PuntoApi {
    @GET("api/puntos") suspend fun listar(): List<PuntoDto>
    @GET("api/puntos/{id}") suspend fun obtener(@Path("id") id: Long): PuntoDto
    @POST("api/puntos") suspend fun crear(@Body punto: PuntoDto): PuntoDto
    @PUT("api/puntos/{id}") suspend fun actualizar(@Path("id") id: Long, @Body punto: PuntoDto): PuntoDto
    @DELETE("api/puntos/{id}") suspend fun eliminar(@Path("id") id: Long)
}