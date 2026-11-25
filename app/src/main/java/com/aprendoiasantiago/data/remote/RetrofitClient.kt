package com.aprendoiasantiago.data.remote

import com.aprendoiasantiago.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Salen de BuildConfig (app/build.gradle.kts): 10.0.2.2 en el emulador,
    // o la IP de backend.host en local.properties para un teléfono físico.
    // "localhost" no sirve: dentro del emulador o del teléfono es el propio aparato.
    private val URL_PUNTOS = BuildConfig.BASE_URL_PUNTOS
    private val URL_JUGADORES = BuildConfig.BASE_URL_JUGADORES

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