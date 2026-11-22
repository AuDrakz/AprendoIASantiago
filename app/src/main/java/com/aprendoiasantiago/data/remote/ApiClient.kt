package com.aprendoiasantiago.data.remote


import com.aprendoiasantiago.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Único lugar donde se construye Retrofit. Las URLs de los microservicios salen de
 * BuildConfig (ver app/build.gradle.kts y backend.host en local.properties).
 * Los timeouts cortos evitan que la app se quede esperando si el servidor no responde.
 */
object ApiClient {

    private const val URL_PHOTON = "https://photon.komoot.io/"

    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            // Photon pide identificar a la aplicación que consulta
            .addInterceptor { cadena ->
                cadena.proceed(
                    cadena.request().newBuilder()
                        .header("User-Agent", "AprendoIASantiago/1.0")
                        .build()
                )
            }
            .build()
    }

    private fun retrofit(urlBase: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(urlBase)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val puntoApi: PuntoApi by lazy { retrofit(BuildConfig.BASE_URL_PUNTOS).create(PuntoApi::class.java) }

    val jugadorApi: JugadorApi by lazy { retrofit(BuildConfig.BASE_URL_JUGADORES).create(JugadorApi::class.java) }

    val photonApi: PhotonApi by lazy { retrofit(URL_PHOTON).create(PhotonApi::class.java) }
}