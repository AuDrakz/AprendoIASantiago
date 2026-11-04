package com.aprendoiasantiago.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class SugerenciaDireccion(
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double
)

/** Busca lugares y calles en Chile con Photon (OpenStreetMap). No necesita API key. */
object BuscadorDirecciones {

    suspend fun buscar(consulta: String): List<SugerenciaDireccion> = withContext(Dispatchers.IO) {
        val texto = consulta.trim()
        if (texto.length < 3) return@withContext emptyList()

        try {
            val q = URLEncoder.encode(texto, "UTF-8")
            // lat/lon: prioriza resultados cerca de Santiago
            val url = URL("https://photon.komoot.io/api/?q=$q&limit=10&lat=-33.4489&lon=-70.6693")
            val conexion = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "AprendoIASantiago/1.0")
            }
            try {
                if (conexion.responseCode != 200) return@withContext emptyList()
                val cuerpo = conexion.inputStream.bufferedReader().use { it.readText() }
                parsear(JSONObject(cuerpo).getJSONArray("features"))
            } finally {
                conexion.disconnect()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parsear(resultados: JSONArray): List<SugerenciaDireccion> {
        val lista = mutableListOf<SugerenciaDireccion>()

        for (i in 0 until resultados.length()) {
            val item = resultados.getJSONObject(i)
            val props = item.getJSONObject("properties")
            if (props.optString("country") != "Chile") continue

            val coords = item.getJSONObject("geometry").getJSONArray("coordinates")
            val calle = listOf(props.optString("street"), props.optString("housenumber"))
                .filter { it.isNotBlank() }
                .joinToString(" ")
            val nombre = props.optString("name").ifBlank { calle }
            if (nombre.isBlank()) continue

            val zona = listOf(
                props.optString("district"),
                props.optString("city"),
                props.optString("state")
            ).filter { it.isNotBlank() }.distinct().joinToString(", ")

            val direccion = listOf(calle, zona)
                .filter { it.isNotBlank() && it != nombre }
                .joinToString(", ")

            lista.add(
                SugerenciaDireccion(
                    nombre = nombre,
                    direccion = direccion,
                    latitud = coords.getDouble(1),
                    longitud = coords.getDouble(0)
                )
            )
        }
        return lista
    }
}