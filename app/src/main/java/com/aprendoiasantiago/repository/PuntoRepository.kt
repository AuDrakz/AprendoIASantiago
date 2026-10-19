
package com.aprendoiasantiago.repository

import com.aprendoiasantiago.data.local.PerfilDao
import com.aprendoiasantiago.data.local.PerfilEntity
import com.aprendoiasantiago.data.local.PuntoDao
import com.aprendoiasantiago.data.local.RankingDao
import com.aprendoiasantiago.data.local.RankingEntity
import com.aprendoiasantiago.data.local.toEntity
import com.aprendoiasantiago.data.local.toModel
import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.model.Rol
import com.aprendoiasantiago.model.ResultadoEscaneo
import com.aprendoiasantiago.util.ArchivosFoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PuntoRepository(
    private val puntoDao: PuntoDao,
    private val perfilDao: PerfilDao,
    private val rankingDao: RankingDao
) {

    val puntos: Flow<List<PuntoInteres>> =
        puntoDao.observarPuntos().map { lista ->
            lista.map { it.toModel() }
        }

    val rankingBase: Flow<List<EntradaRanking>> =
        rankingDao.observarRanking().map { lista ->
            lista.map { it.toModel() }
        }

    val perfil: Flow<PerfilUsuario> = combine(
        perfilDao.observarPerfil(PERFIL_ID).filterNotNull(),
        puntoDao.observarPuntos()
    ) { perfilGuardado, listaPuntos ->

        val visitados = listaPuntos.filter { it.fueVisitado }

        PerfilUsuario(
            id = perfilGuardado.id,
            nombreUsuario = perfilGuardado.nombreUsuario,
            email = perfilGuardado.email,
            rol = perfilGuardado.rol,
            fotoRuta = perfilGuardado.fotoRuta,
            puntosTotales = visitados.sumOf {
                it.puntosRecompensa
            },
            lugaresDescubiertos = visitados.size
        )
    }

    suspend fun sembrarDatosIniciales() {
        if (puntoDao.contar() == 0) {
            puntoDao.insertarTodos(
                puntosIniciales.map { it.toEntity() }
            )
        }

        if (perfilDao.contar() == 0) {
            perfilDao.guardar(
                PerfilEntity(
                    PERFIL_ID,
                    "EstudianteIA",
                    "estudiante@duoc.cl"
                )
            )
        }

        if (rankingDao.contar() == 0) {
            rankingDao.insertarTodos(
                listOf(
                    RankingEntity(nombre = "Camila R.", puntos = 450),
                    RankingEntity(nombre = "Matias R.", puntos = 300),
                    RankingEntity(nombre = "Valentina S.", puntos = 150)
                )
            )
        }
    }

    suspend fun actualizarPerfil(
        nombreUsuario: String,
        email: String,
        rol: Rol,
        fotoRuta: String?
    ) {
        val anterior = perfilDao.fotoActual(PERFIL_ID)

        perfilDao.guardar(
            PerfilEntity(
                PERFIL_ID,
                nombreUsuario.trim(),
                email.trim(),
                rol,
                fotoRuta
            )
        )

        if (anterior != fotoRuta) {
            ArchivosFoto.borrar(anterior)
        }
    }

    suspend fun guardarSelfie(
        puntoId: Int,
        ruta: String
    ) {
        val anterior = puntoDao.selfieActual(puntoId)

        puntoDao.guardarSelfie(puntoId, ruta)

        if (anterior != ruta) {
            ArchivosFoto.borrar(anterior)
        }
    }

    suspend fun registrarVisita(
        codigoQR: String,
        latitudUsuario: Double?,
        longitudUsuario: Double?,
        altitudUsuario: Double? = null
    ): ResultadoEscaneo {

        val entidad = puntoDao.buscarPorQr(codigoQR)
            ?: return ResultadoEscaneo.Invalido

        if (
            latitudUsuario == null ||
            longitudUsuario == null ||
            !latitudUsuario.isFinite() ||
            !longitudUsuario.isFinite() ||
            latitudUsuario !in -90.0..90.0 ||
            longitudUsuario !in -180.0..180.0
        ) {
            return ResultadoEscaneo.SinUbicacion
        }

        val punto = entidad.toModel()

        val distancia = distanciaEnMetros(
            latitudUsuario,
            longitudUsuario,
            punto.latitud,
            punto.longitud
        )

        if (distancia > RADIO_MAXIMO_METROS) {
            return ResultadoEscaneo.FueraDeRango(
                punto = punto,
                distanciaMetros = distancia
            )
        }

        if (entidad.fueVisitado) {
            return ResultadoEscaneo.YaVisitado(punto)
        }

        val altitudValida = altitudUsuario?.takeIf {
            it.isFinite()
        }

        val fechaVisita = java.time.Instant.now().toString()

        val filasActualizadas = puntoDao.marcarVisitado(
            id = entidad.id,
            fecha = fechaVisita,
            altitud = altitudValida
        )

        return if (filasActualizadas > 0) {
            ResultadoEscaneo.Exito(
                entidad.copy(
                    fueVisitado = true,
                    fechaVisita = fechaVisita,
                    altitudVisita = altitudValida
                ).toModel()
            )
        } else {
            ResultadoEscaneo.YaVisitado(
                entidad.copy(fueVisitado = true).toModel()
            )
        }
    }

    private fun distanciaEnMetros(
        latitud1: Double,
        longitud1: Double,
        latitud2: Double,
        longitud2: Double
    ): Float {

        val radioTierra = 6_371_000.0

        val diferenciaLatitud =
            Math.toRadians(latitud2 - latitud1)

        val diferenciaLongitud =
            Math.toRadians(longitud2 - longitud1)

        val senoLatitud = sin(diferenciaLatitud / 2)
        val senoLongitud = sin(diferenciaLongitud / 2)

        val a = (
                senoLatitud * senoLatitud +
                        cos(Math.toRadians(latitud1)) *
                        cos(Math.toRadians(latitud2)) *
                        senoLongitud * senoLongitud
                ).coerceIn(0.0, 1.0)

        val distancia = 2 * radioTierra * atan2(
            sqrt(a),
            sqrt(1.0 - a)
        )

        return distancia.toFloat()
    }

    private companion object {

        const val PERFIL_ID = 1
        const val RADIO_MAXIMO_METROS = 50.0

        val puntosIniciales = listOf(
            PuntoInteres(
                id = 1,
                titulo = "Palacio de La Moneda",
                conceptoIA = "Visión por Computador",
                latitud = -33.4429,
                longitud = -70.6539,
                codigoQR = "QR_LAMONEDA_01",
                puntosRecompensa = 100,
                urlVideo = "https://aprendoia.cl/videos/vision_computador.mp4"
            ),
            PuntoInteres(
                id = 2,
                titulo = "Plaza de Armas",
                conceptoIA = "Procesamiento de Lenguaje Natural",
                latitud = -33.4378,
                longitud = -70.6504,
                codigoQR = "QR_PLAZA_02",
                puntosRecompensa = 150,
                urlVideo = "https://aprendoia.cl/videos/nlp.mp4"
            ),
            PuntoInteres(
                id = 3,
                titulo = "Parque Forestal",
                conceptoIA = "Redes Neuronales",
                latitud = -33.4350,
                longitud = -70.6420,
                codigoQR = "QR_FORESTAL_03",
                puntosRecompensa = 200,
                urlVideo = "https://aprendoia.cl/videos/redes_neuronales.mp4"
            )
        )
    }
}
