package com.aprendoiasantiago.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val URL_PUNTOS = "http://localhost:8081/"
    private const val URL_JUGADORES = "http://localhost:8082/"

    private const val URL_PHOTON = "https://photon.komoot.io/"
    private val clienteHttp by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private fun crear(url: String) = Retrofit.Builder()
        .baseUrl(url)
        .client(clienteHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val puntoApi: PuntoApi by lazy { crear(URL_PUNTOS).create(PuntoApi::class.java) }
    val jugadorApi: JugadorApi by lazy { crear(URL_JUGADORES).create(JugadorApi::class.java) }
    val photonService: RetrofitService by lazy { crear(URL_PHOTON).create(RetrofitService::class.java) }
}

