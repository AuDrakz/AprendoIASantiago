package com.aprendoiasantiago.data.remote


import retrofit2.http.GET
import retrofit2.http.Query

/** API externa Photon (OpenStreetMap): busca lugares y calles. No necesita API key. */
interface PhotonApi {

    @GET("api/")
    suspend fun buscar(
        @Query("q") consulta: String,
        @Query("limit") limite: Int,
        @Query("lat") latitud: Double,
        @Query("lon") longitud: Double
    ): PhotonRespuesta
}

data class PhotonRespuesta(val features: List<PhotonFeature>? = null)

data class PhotonFeature(
    val properties: PhotonPropiedades? = null,
    val geometry: PhotonGeometria? = null
)

data class PhotonPropiedades(
    val name: String? = null,
    val street: String? = null,
    val housenumber: String? = null,
    val district: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null
)

/** GeoJSON: coordinates = [longitud, latitud]. */
data class PhotonGeometria(val coordinates: List<Double>? = null)