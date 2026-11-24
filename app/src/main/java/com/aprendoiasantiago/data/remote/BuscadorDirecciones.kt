package com.aprendoiasantiago.data.remote

import kotlinx.coroutines.CancellationException

data class SugerenciaDireccion(
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double
)

/** Busca lugares y calles en Chile usando RetrofitService (Photon). */
object BuscadorDirecciones {

    // var para poder cambiarlo por un mock en los tests
    var servicio: RetrofitService = RetrofitClient.photonService

    suspend fun buscar(consulta: String): List<SugerenciaDireccion> {
        val texto = consulta.trim()
        if (texto.length < 3) return emptyList()

        return try {
            servicio.buscar(texto).features.mapNotNull { it.aSugerencia() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun PhotonFeature.aSugerencia(): SugerenciaDireccion? {
        if (properties.country != "Chile") return null
        val lon = geometry.coordinates.getOrNull(0) ?: return null
        val lat = geometry.coordinates.getOrNull(1) ?: return null

        val calle = listOfNotNull(properties.street, properties.housenumber)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        val nombre = properties.name?.takeIf { it.isNotBlank() } ?: calle
        if (nombre.isBlank()) return null

        val zona = listOfNotNull(properties.district, properties.city, properties.state)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
        val direccion = listOf(calle, zona)
            .filter { it.isNotBlank() && it != nombre }
            .joinToString(", ")

        return SugerenciaDireccion(nombre, direccion, lat, lon)
    }
}