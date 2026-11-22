package com.aprendoiasantiago.data.remote

import kotlinx.coroutines.CancellationException

data class SugerenciaDireccion(
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double
)

/** Busca lugares y calles en Chile con Photon (OpenStreetMap), ahora a través de Retrofit. */
object BuscadorDirecciones {

    // Centro de Santiago: prioriza los resultados cercanos
    private const val LATITUD_SANTIAGO = -33.4489
    private const val LONGITUD_SANTIAGO = -70.6693

    suspend fun buscar(
        consulta: String,
        api: PhotonApi = ApiClient.photonApi
    ): List<SugerenciaDireccion> {
        val texto = consulta.trim()
        if (texto.length < 3) return emptyList()

        return try {
            aSugerencias(api.buscar(texto, 10, LATITUD_SANTIAGO, LONGITUD_SANTIAGO))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Convierte la respuesta GeoJSON en sugerencias. Solo deja resultados de Chile. */
    internal fun aSugerencias(respuesta: PhotonRespuesta): List<SugerenciaDireccion> {
        val lista = mutableListOf<SugerenciaDireccion>()

        for (item in respuesta.features.orEmpty()) {
            val props = item.properties ?: continue
            if (props.country != "Chile") continue

            val coords = item.geometry?.coordinates ?: continue
            if (coords.size < 2) continue

            val calle = listOf(props.street, props.housenumber)
                .filter { !it.isNullOrBlank() }
                .joinToString(" ")
            val nombre = props.name?.takeIf { it.isNotBlank() } ?: calle
            if (nombre.isBlank()) continue

            val zona = listOf(props.district, props.city, props.state)
                .filter { !it.isNullOrBlank() }
                .distinct()
                .joinToString(", ")

            val direccion = listOf(calle, zona)
                .filter { it.isNotBlank() && it != nombre }
                .joinToString(", ")

            lista.add(
                SugerenciaDireccion(
                    nombre = nombre,
                    direccion = direccion,
                    latitud = coords[1],
                    longitud = coords[0]
                )
            )
        }
        return lista
    }
}