package com.aprendoiasantiago.data.remote

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/** API externa: Photon (OpenStreetMap). Busca lugares y calles. */
interface RetrofitService {
    @Headers("User-Agent: AprendoIASantiago/1.0")
    @GET("api/")
    suspend fun buscar(
        @Query("q") consulta: String,
        @Query("limit") limite: Int = 10,
        @Query("lat") lat: Double = -33.4489,
        @Query("lon") lon: Double = -70.6693
    ): PhotonRespuesta
}

data class PhotonRespuesta(val features: List<PhotonFeature> = emptyList())
data class PhotonFeature(val properties: PhotonProps, val geometry: PhotonGeometry)
data class PhotonGeometry(val coordinates: List<Double> = emptyList())
data class PhotonProps(
    val name: String? = null,
    val street: String? = null,
    val housenumber: String? = null,
    val district: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null
)