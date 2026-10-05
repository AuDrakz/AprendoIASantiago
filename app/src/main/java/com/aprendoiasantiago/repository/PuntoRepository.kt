package com.aprendoiasantiago.repository

import com.aprendoiasantiago.data.local.PerfilDao
import com.aprendoiasantiago.data.local.PerfilEntity
import com.aprendoiasantiago.data.local.PuntoDao
import com.aprendoiasantiago.data.local.toEntity
import com.aprendoiasantiago.data.local.toModel
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.model.ResultadoEscaneo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map


/**
 * Fuente única de verdad: el ViewModel solo habla con este repositorio
 * y no sabe si los datos vienen de Room, de una API o de otro lugar.
 */
class PuntoRepository(
    private val puntoDao: PuntoDao,
    private val perfilDao: PerfilDao
) {

    /** Lista de puntos, siempre actualizada. */
    val puntos: Flow<List<PuntoInteres>> =
        puntoDao.observarPuntos().map { lista -> lista.map { it.toModel() } }

    /**
     * Perfil completo. Los puntos y lugares se CALCULAN a partir de los
     * puntos visitados: no se guardan, así no hay datos duplicados.
     */
    val perfil: Flow<PerfilUsuario> = combine(
        perfilDao.observarPerfil(PERFIL_ID).filterNotNull(),
        puntoDao.observarPuntos()
    ) { perfilGuardado, listaPuntos ->
        val visitados = listaPuntos.filter { it.fueVisitado }
        PerfilUsuario(
            id = perfilGuardado.id,
            nombreUsuario = perfilGuardado.nombreUsuario,
            email = perfilGuardado.email,
            puntosTotales = visitados.sumOf { it.puntosRecompensa },
            lugaresDescubiertos = visitados.size
        )
    }

    /** Primera vez que se abre la app: carga los puntos y el perfil. */
    suspend fun sembrarDatosIniciales() {
        if (puntoDao.contar() == 0) {
            puntoDao.insertarTodos(puntosIniciales.map { it.toEntity() })
        }
        if (perfilDao.contar() == 0) {
            perfilDao.guardar(
                PerfilEntity(PERFIL_ID, "EstudianteIA", "estudiante@duoc.cl")
            )
        }
    }

    /**
     * Valida el código QR contra la base de datos.
     * Si es de un punto nuevo, lo marca como visitado.
     */
    suspend fun registrarVisita(codigoQR: String): ResultadoEscaneo {
        val entidad = puntoDao.buscarPorQr(codigoQR) ?: return ResultadoEscaneo.Invalido
        if (entidad.fueVisitado) return ResultadoEscaneo.YaVisitado(entidad.toModel())

        val actualizado = entidad.copy(fueVisitado = true)
        puntoDao.actualizar(actualizado)
        return ResultadoEscaneo.Exito(actualizado.toModel())
    }

    private companion object {
        const val PERFIL_ID = 1

        val puntosIniciales = listOf(
            PuntoInteres(
                id = 1, titulo = "Palacio de La Moneda", conceptoIA = "Visión por Computador",
                latitud = -33.4429, longitud = -70.6539, codigoQR = "QR_LAMONEDA_01",
                puntosRecompensa = 100, urlVideo = "https://aprendoia.cl/videos/vision_computador.mp4"
            ),
            PuntoInteres(
                id = 2, titulo = "Plaza de Armas", conceptoIA = "Procesamiento de Lenguaje Natural",
                latitud = -33.4378, longitud = -70.6504, codigoQR = "QR_PLAZA_02",
                puntosRecompensa = 150, urlVideo = "https://aprendoia.cl/videos/nlp.mp4"
            ),
            PuntoInteres(
                id = 3, titulo = "Parque Forestal", conceptoIA = "Redes Neuronales",
                latitud = -33.4350, longitud = -70.6420, codigoQR = "QR_FORESTAL_03",
                puntosRecompensa = 200, urlVideo = "https://aprendoia.cl/videos/redes_neuronales.mp4"
            )
        )
    }
}