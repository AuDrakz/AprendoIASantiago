package com.aprendoiasantiago.data.remote


import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Contrato de ms-jugadores (puerto 8082): jugadores, visitas y ranking. */
interface JugadorApi {

    @GET("api/jugadores")
    suspend fun listar(): List<JugadorDto>

    @POST("api/jugadores")
    suspend fun crear(@Body jugador: JugadorDto): JugadorDto

    @PUT("api/jugadores/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body jugador: JugadorDto): JugadorDto

    @DELETE("api/jugadores/{id}")
    suspend fun eliminar(@Path("id") id: Long)

    /** Devuelve el jugador con los puntos actualizados. 409 si ya visitó ese punto. */
    @POST("api/jugadores/{id}/visitas")
    suspend fun registrarVisita(
        @Path("id") jugadorId: Long,
        @Body visita: VisitaRequestDto
    ): JugadorDto

    @GET("api/ranking")
    suspend fun ranking(): List<EntradaRankingDto>
}