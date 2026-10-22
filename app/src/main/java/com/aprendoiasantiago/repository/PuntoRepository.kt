package com.aprendoiasantiago.repository

import com.aprendoiasantiago.data.local.PerfilDao
import com.aprendoiasantiago.data.local.PerfilEntity
import com.aprendoiasantiago.data.local.PuntoDao
import com.aprendoiasantiago.data.local.RankingDao
import com.aprendoiasantiago.data.local.RankingEntity
import com.aprendoiasantiago.data.local.SesionEntity
import com.aprendoiasantiago.data.local.toEntity
import com.aprendoiasantiago.data.local.toModel
import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.model.ResultadoEscaneo
import com.aprendoiasantiago.model.Rol
import com.aprendoiasantiago.util.ArchivosFoto
import com.aprendoiasantiago.util.Seguridad
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

    /** Perfil del jugador con la sesión abierta (id = 0 si no hay sesión). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val perfil: Flow<PerfilUsuario> = perfilDao.observarSesion().flatMapLatest { jugadorId ->
        if (jugadorId == null) {
            flowOf(PerfilUsuario(id = 0, nombreUsuario = "", email = ""))
        } else {
            combine(
                perfilDao.observarPerfil(jugadorId).filterNotNull(),
                puntoDao.observarPuntos()
            ) { perfilGuardado, listaPuntos ->
                val visitados = listaPuntos.filter { it.fueVisitado }

                perfilGuardado.toModel(
                    puntosTotales = visitados.sumOf { it.puntosRecompensa },
                    lugaresDescubiertos = visitados.size
                )
            }
        }
    }

    /** Jugador con la sesión abierta, o null si no hay sesión. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val jugadorActivo: Flow<PerfilUsuario?> = perfilDao.observarSesion().flatMapLatest { jugadorId ->
        if (jugadorId == null) {
            flowOf(null)
        } else {
            perfilDao.observarPerfil(jugadorId).map { it?.toModel() }
        }
    }

    /** Todos los jugadores del dispositivo (para la pantalla de selección). */
    val jugadores: Flow<List<PerfilUsuario>> =
        perfilDao.observarJugadores().map { lista ->
            lista.map { it.toModel() }
        }

    suspend fun sembrarDatosIniciales() {
        if (puntoDao.contar() == 0) {
            puntoDao.insertarTodos(puntosIniciales.map { it.toEntity() })
        } else {
            // Si la app ya tenía una base de datos, añade solo los puntos que falten.
            // IGNORE evita reemplazar los puntos visitados o perder sus recompensas.
            puntosIniciales.forEach { punto ->
                puntoDao.insertarSiNoExiste(punto.toEntity())
            }
        }

        // Asegura que los QR impresos coincidan con los códigos consultados por el escáner.
        // Solo cambia codigoQR en los puntos iniciales; conserva visita, fecha y altitud.
        puntosIniciales.forEach { punto ->
            puntoDao.actualizarCodigoQr(
                id = punto.id,
                titulo = punto.titulo,
                codigoQR = punto.codigoQR
            )
        }

        // Cuentas en duro exigidas por el caso: Administrador y Creador de Contenido
        if (perfilDao.contar() == 0) {
            perfilDao.insertar(
                PerfilEntity(
                    nombreUsuario = "Administrador",
                    alias = "Admin",
                    email = "admin@aprendoia.cl",
                    avatar = 5,
                    contrasenaHash = Seguridad.hashear("123"),
                    rol = Rol.ADMINISTRADOR
                )
            )
            perfilDao.insertar(
                PerfilEntity(
                    nombreUsuario = "Contenido",
                    alias = "GenCont",
                    email = "contenido@aprendoia.cl",
                    avatar = 4,
                    contrasenaHash = Seguridad.hashear("123"),
                    rol = Rol.GENERADOR
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

    /** Crea un Cliente y abre su sesión. Devuelve false si el correo ya existe. */
    suspend fun registrarJugador(
        nombre: String,
        email: String,
        telefono: String,
        direccion: String,
        avatar: Int,
        contrasena: String
    ): Boolean {
        if (perfilDao.contarPorEmail(email.trim(), excluirId = 0) > 0) return false

        val id = perfilDao.insertar(
            PerfilEntity(
                nombreUsuario = nombre.trim(),
                email = email.trim(),
                telefono = telefono.trim(),
                direccion = direccion.trim(),
                avatar = avatar,
                contrasenaHash = Seguridad.hashear(contrasena)
            )
        )
        perfilDao.guardarSesion(SesionEntity(jugadorId = id.toInt()))
        return true
    }

    /** Abre la sesión si la contraseña es correcta. */
    suspend fun iniciarSesion(jugadorId: Int, contrasena: String): Boolean {
        val jugador = perfilDao.buscarPorId(jugadorId) ?: return false
        if (!Seguridad.verificar(contrasena, jugador.contrasenaHash)) return false

        perfilDao.guardarSesion(SesionEntity(jugadorId = jugadorId))
        return true
    }

    suspend fun cerrarSesion() = perfilDao.cerrarSesion()

    // --- Gestión (Administrador y Creador de Contenido) ---

    /**
     * Crea (id = 0) o actualiza un punto.
     * Devuelve false si el código QR ya lo usa otro punto.
     */
    suspend fun guardarPunto(punto: PuntoInteres): Boolean {
        val qr = punto.codigoQR.trim()
        val existente = puntoDao.buscarPorQr(qr)
        if (existente != null && existente.id != punto.id) return false

        if (punto.id == 0) {
            puntoDao.insertar(
                punto.copy(id = puntoDao.maxId() + 1, codigoQR = qr).toEntity()
            )
        } else {
            puntoDao.actualizar(punto.copy(codigoQR = qr).toEntity())
        }
        return true
    }

    suspend fun eliminarPunto(id: Int) {
        val selfie = puntoDao.selfieActual(id)
        puntoDao.eliminar(id)
        ArchivosFoto.borrar(selfie)
    }

    /** Solo se pueden eliminar Clientes; las cuentas en duro quedan protegidas. */
    suspend fun eliminarJugador(id: Int) {
        val jugador = perfilDao.buscarPorId(id) ?: return
        if (jugador.rol != Rol.CLIENTE) return

        perfilDao.eliminar(id)
        ArchivosFoto.borrar(jugador.fotoRuta)
    }

    /** Edita nombre y correo de un Cliente. Devuelve false si el correo ya lo usa otro. */
    suspend fun actualizarJugador(
        id: Int,
        nombreUsuario: String,
        email: String
    ): Boolean {
        val actual = perfilDao.buscarPorId(id) ?: return false
        if (actual.rol != Rol.CLIENTE) return false
        if (perfilDao.contarPorEmail(email.trim(), id) > 0) return false

        perfilDao.actualizar(
            actual.copy(
                nombreUsuario = nombreUsuario.trim(),
                email = email.trim()
            )
        )
        return true
    }

    /** Actualiza al jugador activo. Devuelve false si el correo ya lo usa otro jugador. */
    suspend fun actualizarPerfil(
        nombreUsuario: String,
        email: String,
        fotoRuta: String?
    ): Boolean {
        val jugadorId = perfilDao.sesionActual() ?: return false
        val actual = perfilDao.buscarPorId(jugadorId) ?: return false

        if (perfilDao.contarPorEmail(email.trim(), jugadorId) > 0) return false

        perfilDao.actualizar(
            actual.copy(
                nombreUsuario = nombreUsuario.trim(),
                email = email.trim(),
                fotoRuta = fotoRuta
            )
        )

        if (actual.fotoRuta != fotoRuta) {
            ArchivosFoto.borrar(actual.fotoRuta)
        }
        return true
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