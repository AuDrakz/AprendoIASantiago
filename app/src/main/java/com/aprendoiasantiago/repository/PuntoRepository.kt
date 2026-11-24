package com.aprendoiasantiago.repository

import com.aprendoiasantiago.data.local.PerfilDao
import com.aprendoiasantiago.data.local.PerfilEntity
import com.aprendoiasantiago.data.local.PuntoDao
import com.aprendoiasantiago.data.local.RankingDao
import com.aprendoiasantiago.data.local.RankingEntity
import com.aprendoiasantiago.data.local.SesionDao
import com.aprendoiasantiago.data.local.SesionEntity
import com.aprendoiasantiago.data.local.VisitaDao
import com.aprendoiasantiago.data.local.VisitaEntity
import com.aprendoiasantiago.data.local.toEntity
import com.aprendoiasantiago.data.local.toModel
import com.aprendoiasantiago.data.remote.JugadorApi
import com.aprendoiasantiago.data.remote.JugadorDto
import com.aprendoiasantiago.data.remote.PuntoApi
import com.aprendoiasantiago.data.remote.RetrofitClient
import com.aprendoiasantiago.data.remote.VisitaRequestDto
import com.aprendoiasantiago.data.remote.aDto
import com.aprendoiasantiago.data.remote.aModelo
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PuntoRepository(
    private val puntoDao: PuntoDao,
    private val perfilDao: PerfilDao,
    private val rankingDao: RankingDao,
    private val sesionDao: SesionDao,
    private val visitaDao: VisitaDao,
    private val puntoApi: PuntoApi = RetrofitClient.puntoApi,
    private val jugadorApi: JugadorApi = RetrofitClient.jugadorApi
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val puntos: Flow<List<PuntoInteres>> =
        sesionDao.observarSesion().flatMapLatest { jugadorId ->
            val visitas = if (jugadorId == null) flowOf(emptyList())
            else visitaDao.observarVisitas(jugadorId)
            combine(puntoDao.observarPuntos(), visitas) { lista, listaVisitas ->
                val porPunto = listaVisitas.associateBy { it.puntoId }
                lista.map { entidad ->
                    val v = porPunto[entidad.id]
                    entidad.toModel().copy(
                        fueVisitado = v != null,
                        selfieRuta = v?.selfieRuta,
                        fechaVisita = v?.fecha,
                        altitudVisita = v?.altitud
                    )
                }
            }
        }

    val rankingBase: Flow<List<EntradaRanking>> =
        rankingDao.observarRanking().map { lista ->
            lista.map { it.toModel() }
        }

    /** Perfil del jugador con la sesión abierta (id = 0 si no hay sesión). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val perfil: Flow<PerfilUsuario> = sesionDao.observarSesion().flatMapLatest { jugadorId ->
        if (jugadorId == null) {
            flowOf(PerfilUsuario(id = 0, nombreUsuario = "", email = ""))
        } else {
            combine(
                perfilDao.observarPerfil(jugadorId).filterNotNull(),
                puntos
            ) { perfilGuardado, listaPuntos ->
                val visitados = listaPuntos.filter { it.fueVisitado }

                perfilGuardado.toModel(
                    puntosTotales = visitados.sumOf { it.puntosRecompensa } +
                            visitados.count { it.selfieRuta != null } * PUNTOS_SELFIE,
                    lugaresDescubiertos = visitados.size
                )
            }
        }
    }

    /** Jugador con la sesión abierta, o null si no hay sesión. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val jugadorActivo: Flow<PerfilUsuario?> = sesionDao.observarSesion().flatMapLatest { jugadorId ->
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

    /** Crea un Cliente en ms-jugadores y en Room (con el mismo id) y abre su sesión. */
    suspend fun registrarJugador(
        nombre: String,
        email: String,
        avatar: Int,
        contrasena: String
    ): ResultadoRegistro {
        if (perfilDao.contarPorEmail(email.trim(), excluirId = 0) > 0) {
            return ResultadoRegistro.CORREO_DUPLICADO
        }
        if (perfilDao.contarPorNombre(nombre.trim()) > 0) {
            return ResultadoRegistro.NOMBRE_DUPLICADO
        }

        val creado = try {
            jugadorApi.crear(
                JugadorDto(
                    nombreUsuario = nombre.trim(),
                    email = email.trim(),
                    rol = Rol.CLIENTE.name
                )
            )
        } catch (e: HttpException) {
            if (e.code() == 409) {
                val mensaje = e.response()?.errorBody()?.string().orEmpty()
                return if (mensaje.contains("correo", ignoreCase = true)) {
                    ResultadoRegistro.CORREO_DUPLICADO
                } else {
                    ResultadoRegistro.NOMBRE_DUPLICADO
                }
            }
            throw e
        }

        val id = (creado.id ?: 0L).toInt()
        perfilDao.insertar(
            PerfilEntity(
                id = id,
                nombreUsuario = nombre.trim(),
                email = email.trim(),
                avatar = avatar,
                contrasenaHash = Seguridad.hashear(contrasena)
            )
        )
        sesionDao.guardarSesion(SesionEntity(jugadorId = id))
        return ResultadoRegistro.EXITO
    }

    /** Abre la sesión si la contraseña es correcta. */
    suspend fun iniciarSesion(jugadorId: Int, contrasena: String): Boolean {
        val jugador = perfilDao.buscarPorId(jugadorId) ?: return false
        if (!Seguridad.verificar(contrasena, jugador.contrasenaHash)) return false

        sesionDao.guardarSesion(SesionEntity(jugadorId = jugadorId))
        return true
    }

    /** Abre la sesión con nombre de usuario, alias o correo, más la contraseña. */
    suspend fun iniciarSesionConCredencial(usuario: String, contrasena: String): Boolean {
        val jugador = perfilDao.buscarPorCredencial(usuario.trim()) ?: return false
        if (!Seguridad.verificar(contrasena, jugador.contrasenaHash)) return false

        sesionDao.guardarSesion(SesionEntity(jugadorId = jugador.id))
        return true
    }

    suspend fun cerrarSesion() = sesionDao.cerrarSesion()

    // --- Gestión (Administrador y Creador de Contenido) ---

    /** Crea (id = 0) o actualiza un punto en ms-puntos y lo guarda en Room. false = QR repetido (409). */
    suspend fun guardarPunto(punto: PuntoInteres): Boolean {
        val limpio = punto.copy(codigoQR = punto.codigoQR.trim())
        return try {
            val guardado = if (limpio.id == 0) {
                puntoApi.crear(limpio.aDto())
            } else {
                puntoApi.actualizar(limpio.id.toLong(), limpio.aDto())
            }.aModelo()

            val local = puntoDao.buscarPorId(guardado.id)
            puntoDao.insertar(
                guardado.copy(
                    fueVisitado = local?.fueVisitado ?: false,
                    selfieRuta = local?.selfieRuta,
                    fechaVisita = local?.fechaVisita,
                    altitudVisita = local?.altitudVisita
                ).toEntity()
            )
            true
        } catch (e: HttpException) {
            if (e.code() == 409) false else throw e
        }
    }

    suspend fun eliminarPunto(id: Int) {
        val selfies = visitaDao.selfiesDePunto(id)
        try {
            puntoApi.eliminar(id.toLong())
        } catch (e: HttpException) {
            if (e.code() != 404) throw e
        }
        puntoDao.eliminar(id)
        visitaDao.eliminarPorPunto(id)
        selfies.forEach { ArchivosFoto.borrar(it) }
    }
    /** Baja los puntos de ms-puntos y los guarda en Room sin perder visitas ni selfies. */
    suspend fun sincronizarPuntos() {
        val remotos = puntoApi.listar().map { it.aModelo() }
        remotos.forEach { remoto ->
            val local = puntoDao.buscarPorId(remoto.id)
            puntoDao.insertar(
                remoto.copy(
                    fueVisitado = local?.fueVisitado ?: false,
                    selfieRuta = local?.selfieRuta,
                    fechaVisita = local?.fechaVisita,
                    altitudVisita = local?.altitudVisita
                ).toEntity()
            )
        }
        val idsRemotos = remotos.map { it.id }.toSet()
        puntoDao.observarPuntos().first()
            .filter { it.id !in idsRemotos }
            .forEach { puntoDao.eliminar(it.id) }
    }

    /** Baja el ranking de ms-jugadores y lo guarda en Room. */
    suspend fun sincronizarRanking() {
        val remoto = jugadorApi.ranking()
        rankingDao.borrarTodo()
        rankingDao.insertarTodos(remoto.map { RankingEntity(nombre = it.nombre, puntos = it.puntos) })
    }

    /** Solo se pueden eliminar Clientes; las cuentas en duro quedan protegidas. */
    suspend fun eliminarJugador(id: Int) {
        val jugador = perfilDao.buscarPorId(id) ?: return
        if (jugador.rol != Rol.CLIENTE) return

        try {
            jugadorApi.eliminar(id.toLong())
        } catch (e: HttpException) {
            if (e.code() != 404) throw e
        }
        val selfies = visitaDao.selfiesDeJugador(id)
        perfilDao.eliminar(id)
        visitaDao.eliminarPorJugador(id)
        selfies.forEach { ArchivosFoto.borrar(it) }
        ArchivosFoto.borrar(jugador.fotoRuta)
    }

    /** Edita nombre y correo de un Cliente. Devuelve false si el correo o nombre ya los usa otro. */
    suspend fun actualizarJugador(
        id: Int,
        nombreUsuario: String,
        email: String
    ): Boolean {
        val actual = perfilDao.buscarPorId(id) ?: return false
        if (actual.rol != Rol.CLIENTE) return false
        if (perfilDao.contarPorEmail(email.trim(), id) > 0) return false

        try {
            jugadorApi.actualizar(
                id.toLong(),
                JugadorDto(
                    nombreUsuario = nombreUsuario.trim(),
                    email = email.trim(),
                    telefono = actual.telefono,
                    rol = actual.rol.name
                )
            )
        } catch (e: HttpException) {
            if (e.code() == 409) return false else throw e
        }

        perfilDao.actualizar(
            actual.copy(
                nombreUsuario = nombreUsuario.trim(),
                email = email.trim()
            )
        )
        return true
    }

    /** Actualiza al jugador activo. Devuelve false si el correo o nombre ya los usa otro jugador. */
    suspend fun actualizarPerfil(
        nombreUsuario: String,
        email: String,
        fotoRuta: String?
    ): Boolean {
        val jugadorId = sesionDao.sesionActual() ?: return false
        val actual = perfilDao.buscarPorId(jugadorId) ?: return false

        if (perfilDao.contarPorEmail(email.trim(), jugadorId) > 0) return false

        try {
            jugadorApi.actualizar(
                jugadorId.toLong(),
                JugadorDto(
                    nombreUsuario = nombreUsuario.trim(),
                    email = email.trim(),
                    telefono = actual.telefono,
                    rol = actual.rol.name
                )
            )
        } catch (e: HttpException) {
            if (e.code() == 409) return false else throw e
        }

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
        val jugadorId = sesionDao.sesionActual() ?: return
        val anterior = visitaDao.selfieActual(jugadorId, puntoId)
        visitaDao.guardarSelfie(jugadorId, puntoId, ruta)

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

        val jugadorId = sesionDao.sesionActual() ?: return ResultadoEscaneo.Invalido

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

        if (visitaDao.buscar(jugadorId, entidad.id) != null) {
            return ResultadoEscaneo.YaVisitado(punto.copy(fueVisitado = true))
        }

        val altitudValida = altitudUsuario?.takeIf {
            it.isFinite()
        }

        val fechaVisita = java.time.Instant.now().toString()

        val filasActualizadas = if (
            visitaDao.insertarSiNoExiste(
                VisitaEntity(jugadorId, entidad.id, fechaVisita, altitudValida)
            ) != -1L
        ) 1 else 0

        if (filasActualizadas > 0) enviarVisita(entidad.id)
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

    /** Visita + selfie. Devuelve el resultado y si se otorgó el bonus de selfie. */
    suspend fun registrarSelfie(
        codigoQR: String,
        latitudUsuario: Double?,
        longitudUsuario: Double?,
        altitudUsuario: Double?,
        ruta: String
    ): Pair<ResultadoEscaneo, Boolean> {
        val resultado = registrarVisita(codigoQR, latitudUsuario, longitudUsuario, altitudUsuario)

        val punto = when (resultado) {
            is ResultadoEscaneo.Exito -> resultado.punto
            is ResultadoEscaneo.YaVisitado -> resultado.punto
            else -> return resultado to false
        }

        val jugadorId = sesionDao.sesionActual() ?: return resultado to false
        if (visitaDao.selfieActual(jugadorId, punto.id) != null) return resultado to false
        visitaDao.guardarSelfie(jugadorId, punto.id, ruta)
        return resultado to true
    }

    /** Avisa a ms-jugadores de la visita y actualiza el ranking. Si falla, la visita local se mantiene. */
    private suspend fun enviarVisita(puntoId: Int) {
        val jugadorId = sesionDao.sesionActual() ?: return
        try {
            jugadorApi.registrarVisita(jugadorId.toLong(), VisitaRequestDto(puntoId.toLong()))
            sincronizarRanking()
        } catch (_: HttpException) {
        } catch (_: IOException) {
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
        const val PUNTOS_SELFIE = 100

        val puntosIniciales = listOf(
            PuntoInteres(
                id = 1,
                titulo = "Palacio de La Moneda",
                conceptoIA = "Visión por Computador",
                latitud = -33.4429,
                longitud = -70.6539,
                codigoQR = "QR_LAMONEDA_01",
                puntosRecompensa = 10,
                urlVideo = "https://aprendoia.cl/videos/vision_computador.mp4"
            ),
            PuntoInteres(
                id = 2,
                titulo = "Plaza de Armas",
                conceptoIA = "Procesamiento de Lenguaje Natural",
                latitud = -33.4378,
                longitud = -70.6504,
                codigoQR = "QR_PLAZA_02",
                puntosRecompensa = 10,
                urlVideo = "https://aprendoia.cl/videos/nlp.mp4"
            ),
            PuntoInteres(
                id = 3,
                titulo = "Parque Forestal",
                conceptoIA = "Redes Neuronales",
                latitud = -33.4350,
                longitud = -70.6420,
                codigoQR = "QR_FORESTAL_03",
                puntosRecompensa = 10,
                urlVideo = "https://aprendoia.cl/videos/redes_neuronales.mp4"
            )
        )
    }
}