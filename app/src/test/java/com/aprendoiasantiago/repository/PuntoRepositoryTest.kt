package com.aprendoiasantiago.repository

import com.aprendoiasantiago.data.local.PerfilDao
import com.aprendoiasantiago.data.local.PerfilEntity
import com.aprendoiasantiago.data.local.PuntoDao
import com.aprendoiasantiago.data.local.PuntoEntity
import com.aprendoiasantiago.data.local.RankingDao
import com.aprendoiasantiago.data.local.RankingEntity
import com.aprendoiasantiago.data.local.SesionDao
import com.aprendoiasantiago.data.local.SesionEntity
import com.aprendoiasantiago.data.local.VisitaDao
import com.aprendoiasantiago.data.local.VisitaEntity
import com.aprendoiasantiago.data.remote.EntradaRankingDto
import com.aprendoiasantiago.data.remote.JugadorApi
import com.aprendoiasantiago.data.remote.JugadorDto
import com.aprendoiasantiago.data.remote.PuntoApi
import com.aprendoiasantiago.data.remote.PuntoDto
import com.aprendoiasantiago.data.remote.VisitaRequestDto
import com.aprendoiasantiago.excepcionDe
import com.aprendoiasantiago.httpError
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.model.ResultadoEscaneo
import com.aprendoiasantiago.model.Rol
import com.aprendoiasantiago.util.Seguridad
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.File
import java.io.IOException

/** Pruebas del repositorio con DAOs y APIs simulados (MockK). */
class PuntoRepositoryTest {

    private val puntoDao = mockk<PuntoDao>(relaxed = true)
    private val perfilDao = mockk<PerfilDao>(relaxed = true)
    private val rankingDao = mockk<RankingDao>(relaxed = true)
    private val sesionDao = mockk<SesionDao>(relaxed = true)
    private val visitaDao = mockk<VisitaDao>(relaxed = true)
    private val puntoApi = mockk<PuntoApi>(relaxed = true)
    private val jugadorApi = mockk<JugadorApi>(relaxed = true)

    private val sesionFlow = MutableStateFlow<Int?>(null)
    private val puntosFlow = MutableStateFlow<List<PuntoEntity>>(emptyList())
    private val visitasFlow = MutableStateFlow<List<VisitaEntity>>(emptyList())
    private val perfilFlow = MutableStateFlow<PerfilEntity?>(null)
    private val jugadoresFlow = MutableStateFlow<List<PerfilEntity>>(emptyList())
    private val rankingFlow = MutableStateFlow<List<RankingEntity>>(emptyList())

    private lateinit var repo: PuntoRepository
    private val temporales = mutableListOf<File>()

    @Before
    fun preparar() {
        every { sesionDao.observarSesion() } returns sesionFlow
        every { puntoDao.observarPuntos() } returns puntosFlow
        every { visitaDao.observarVisitas(any()) } returns visitasFlow
        every { perfilDao.observarPerfil(any()) } returns perfilFlow
        every { perfilDao.observarJugadores() } returns jugadoresFlow
        every { rankingDao.observarRanking() } returns rankingFlow

        coEvery { sesionDao.sesionActual() } returns null
        coEvery { puntoDao.buscarPorId(any()) } returns null
        coEvery { puntoDao.buscarPorQr(any()) } returns null
        coEvery { puntoDao.contar() } returns 3
        coEvery { puntoDao.insertarSiNoExiste(any()) } returns 1L
        coEvery { puntoDao.actualizarCodigoQr(any(), any(), any()) } returns 1
        coEvery { perfilDao.contar() } returns 2
        coEvery { perfilDao.buscarPorId(any()) } returns null
        coEvery { perfilDao.buscarPorCredencial(any()) } returns null
        coEvery { perfilDao.contarPorEmail(any(), any()) } returns 0
        coEvery { perfilDao.contarPorNombre(any()) } returns 0
        coEvery { perfilDao.insertar(any()) } returns 1L
        coEvery { rankingDao.contar() } returns 3
        coEvery { visitaDao.buscar(any(), any()) } returns null
        coEvery { visitaDao.insertarSiNoExiste(any()) } returns 1L
        coEvery { visitaDao.selfieActual(any(), any()) } returns null
        coEvery { visitaDao.selfiesDePunto(any()) } returns emptyList()
        coEvery { visitaDao.selfiesDeJugador(any()) } returns emptyList()
        coEvery { jugadorApi.ranking() } returns emptyList()

        repo = PuntoRepository(
            puntoDao, perfilDao, rankingDao, sesionDao, visitaDao, puntoApi, jugadorApi
        )
    }

    @After
    fun limpiar() {
        temporales.forEach { it.delete() }
    }

    // ---------- helpers ----------

    private fun archivoTemporal(): File =
        File.createTempFile("foto", ".jpg").also { temporales += it }

    private fun punto(
        id: Int = 1,
        qr: String = "QR_LAMONEDA_01",
        visitado: Boolean = false,
        selfie: String? = null
    ) = PuntoEntity(
        id = id, titulo = "Palacio de La Moneda", conceptoIA = "Visión por Computador",
        latitud = -33.4429, longitud = -70.6539, codigoQR = qr, puntosRecompensa = 10,
        urlVideo = "https://aprendoia.cl/v.mp4", fueVisitado = visitado, selfieRuta = selfie
    )

    private fun perfil(
        id: Int = 1,
        rol: Rol = Rol.CLIENTE,
        email: String = "ana@duoc.cl",
        nombre: String = "Ana",
        clave: String = "1234",
        foto: String? = null
    ) = PerfilEntity(
        id = id, nombreUsuario = nombre, email = email,
        contrasenaHash = Seguridad.hashear(clave), rol = rol, fotoRuta = foto
    )

    private fun jugadorDto(id: Long = 7) =
        JugadorDto(id = id, nombreUsuario = "Ana", email = "ana@duoc.cl", rol = "CLIENTE")

    private fun puntoDto(id: Long = 9) = PuntoDto(
        id = id, titulo = "Nuevo", conceptoIA = "IA", latitud = -33.4, longitud = -70.6,
        codigoQR = "QR_NUEVO", puntosRecompensa = 5, urlVideo = "u"
    )

    private fun puntoNuevo(id: Int = 0, qr: String = " QR_NUEVO ") = PuntoInteres(
        id = id, titulo = "Nuevo", conceptoIA = "IA", latitud = -33.4, longitud = -70.6,
        codigoQR = qr, puntosRecompensa = 5, urlVideo = "u"
    )

    // ---------- flujos ----------

    @Test
    fun puntos_sinSesion_vienenSinVisitar() = runTest {
        puntosFlow.value = listOf(punto())
        val lista = repo.puntos.first()
        assertEquals(1, lista.size)
        assertFalse(lista[0].fueVisitado)
    }

    @Test
    fun puntos_conSesion_marcaVisitasSelfieYAltitud() = runTest {
        sesionFlow.value = 1
        puntosFlow.value = listOf(punto(1), punto(2, qr = "QR_2"))
        visitasFlow.value = listOf(VisitaEntity(1, 1, "2026-01-01T00:00:00Z", 560.0, "/s.jpg"))

        val lista = repo.puntos.first()

        assertTrue(lista[0].fueVisitado)
        assertEquals("/s.jpg", lista[0].selfieRuta)
        assertEquals("2026-01-01T00:00:00Z", lista[0].fechaVisita)
        assertEquals(560.0, lista[0].altitudVisita!!, 0.001)
        assertFalse(lista[1].fueVisitado)
    }

    @Test
    fun perfil_sinSesion_entregaPerfilVacio() = runTest {
        val p = repo.perfil.first()
        assertEquals(0, p.id)
        assertEquals("", p.nombreUsuario)
    }

    @Test
    fun perfil_conSesion_sumaPuntosDeVisitasYBonusDeSelfie() = runTest {
        sesionFlow.value = 1
        perfilFlow.value = perfil()
        puntosFlow.value = listOf(punto(1), punto(2, qr = "QR_2"), punto(3, qr = "QR_3"))
        visitasFlow.value = listOf(
            VisitaEntity(1, 1, "f", null, "/s.jpg"),
            VisitaEntity(1, 2, "f", null, null)
        )

        val p = repo.perfil.first()

        assertEquals("Ana", p.nombreUsuario)
        assertEquals(2, p.lugaresDescubiertos)
        // 10 + 10 por dos visitas + 100 por una selfie
        assertEquals(120, p.puntosTotales)
    }

    @Test
    fun jugadorActivo_esNuloSinSesionYTieneDatosConSesion() = runTest {
        assertNull(repo.jugadorActivo.first())
        sesionFlow.value = 1
        perfilFlow.value = perfil()
        assertEquals("Ana", repo.jugadorActivo.first()?.nombreUsuario)
    }

    @Test
    fun rankingBase_yJugadores_conviertenEntidadesAModelo() = runTest {
        rankingFlow.value = listOf(RankingEntity(nombre = "Luis", puntos = 300))
        jugadoresFlow.value = listOf(perfil(), perfil(id = 2, nombre = "Eva", email = "e@d.cl"))

        assertEquals(300, repo.rankingBase.first().single().puntos)
        assertEquals(listOf("Ana", "Eva"), repo.jugadores.first().map { it.nombreUsuario })
    }

    // ---------- sembrar datos ----------

    @Test
    fun sembrar_conBaseVacia_insertaPuntosCuentasYRanking() = runTest {
        coEvery { puntoDao.contar() } returns 0
        coEvery { perfilDao.contar() } returns 0
        coEvery { rankingDao.contar() } returns 0
        val creados = mutableListOf<PerfilEntity>()
        coEvery { perfilDao.insertar(capture(creados)) } returns 1L

        repo.sembrarDatosIniciales()

        coVerify(exactly = 1) { puntoDao.insertarTodos(match { it.size == 3 }) }
        coVerify(exactly = 3) { puntoDao.actualizarCodigoQr(any(), any(), any()) }
        coVerify(exactly = 1) { rankingDao.insertarTodos(match { it.size == 3 }) }
        assertEquals(listOf(Rol.ADMINISTRADOR, Rol.GENERADOR), creados.map { it.rol })
        assertTrue(Seguridad.verificar("123", creados[0].contrasenaHash))
    }

    @Test
    fun sembrar_conDatosExistentes_soloAgregaLosQueFaltan() = runTest {
        repo.sembrarDatosIniciales()

        coVerify(exactly = 3) { puntoDao.insertarSiNoExiste(any()) }
        coVerify(exactly = 0) { puntoDao.insertarTodos(any()) }
        coVerify(exactly = 0) { perfilDao.insertar(any()) }
        coVerify(exactly = 0) { rankingDao.insertarTodos(any()) }
    }

    // ---------- registro y sesión ----------

    @Test
    fun registrarJugador_exito_creaEnBackendGuardaEnRoomYAbreSesion() = runTest {
        coEvery { jugadorApi.crear(any()) } returns jugadorDto(7)
        val guardado = slot<PerfilEntity>()
        coEvery { perfilDao.insertar(capture(guardado)) } returns 1L

        val r = repo.registrarJugador(" Ana ", " ana@duoc.cl ", 3, "1234")

        assertEquals(ResultadoRegistro.EXITO, r)
        assertEquals(7, guardado.captured.id)
        assertEquals("Ana", guardado.captured.nombreUsuario)
        assertEquals(3, guardado.captured.avatar)
        assertNotEquals("1234", guardado.captured.contrasenaHash)
        coVerify { jugadorApi.crear(match { it.rol == "CLIENTE" && it.email == "ana@duoc.cl" }) }
        coVerify { sesionDao.guardarSesion(SesionEntity(jugadorId = 7)) }
    }

    @Test
    fun registrarJugador_correoRepetidoEnRoom_noLlamaAlBackend() = runTest {
        coEvery { perfilDao.contarPorEmail(any(), any()) } returns 1

        assertEquals(
            ResultadoRegistro.CORREO_DUPLICADO,
            repo.registrarJugador("Ana", "ana@duoc.cl", 0, "1234")
        )
        coVerify(exactly = 0) { jugadorApi.crear(any()) }
    }

    @Test
    fun registrarJugador_nombreRepetidoEnRoom_noLlamaAlBackend() = runTest {
        coEvery { perfilDao.contarPorNombre(any()) } returns 1

        assertEquals(
            ResultadoRegistro.NOMBRE_DUPLICADO,
            repo.registrarJugador("Ana", "ana@duoc.cl", 0, "1234")
        )
        coVerify(exactly = 0) { jugadorApi.crear(any()) }
    }

    @Test
    fun registrarJugador_conflictoDelBackend_distingueCorreoDeNombre() = runTest {
        coEvery { jugadorApi.crear(any()) } throws httpError(409, "El correo ya existe")
        assertEquals(
            ResultadoRegistro.CORREO_DUPLICADO,
            repo.registrarJugador("Ana", "ana@duoc.cl", 0, "1234")
        )

        coEvery { jugadorApi.crear(any()) } throws httpError(409, "El nombre ya existe")
        assertEquals(
            ResultadoRegistro.NOMBRE_DUPLICADO,
            repo.registrarJugador("Ana", "ana@duoc.cl", 0, "1234")
        )
    }

    @Test
    fun registrarJugador_errorDelServidor_seRelanza() = runTest {
        coEvery { jugadorApi.crear(any()) } throws httpError(503)

        val e = excepcionDe { repo.registrarJugador("Ana", "ana@duoc.cl", 0, "1234") }

        assertTrue(e is HttpException)
        coVerify(exactly = 0) { sesionDao.guardarSesion(any()) }
    }

    @Test
    fun iniciarSesion_validaExistenciaYContrasena() = runTest {
        assertFalse(repo.iniciarSesion(1, "1234")) // no existe

        coEvery { perfilDao.buscarPorId(1) } returns perfil(clave = "1234")
        assertFalse(repo.iniciarSesion(1, "mala"))
        coVerify(exactly = 0) { sesionDao.guardarSesion(any()) }

        assertTrue(repo.iniciarSesion(1, "1234"))
        coVerify(exactly = 1) { sesionDao.guardarSesion(SesionEntity(jugadorId = 1)) }
    }

    @Test
    fun iniciarSesionConCredencial_recortaElTextoYVerificaLaClave() = runTest {
        assertFalse(repo.iniciarSesionConCredencial("ana", "1234")) // no existe

        coEvery { perfilDao.buscarPorCredencial("ana") } returns perfil(id = 4, clave = "1234")
        assertFalse(repo.iniciarSesionConCredencial("  ana ", "mala"))
        assertTrue(repo.iniciarSesionConCredencial("  ana ", "1234"))
        coVerify { sesionDao.guardarSesion(SesionEntity(jugadorId = 4)) }
    }

    @Test
    fun cerrarSesion_limpiaLaSesion() = runTest {
        repo.cerrarSesion()
        coVerify(exactly = 1) { sesionDao.cerrarSesion() }
    }

    // ---------- gestión de puntos ----------

    @Test
    fun guardarPunto_nuevo_creaEnBackendConQrRecortadoYGuardaEnRoom() = runTest {
        coEvery { puntoApi.crear(any()) } returns puntoDto(9)
        val guardado = slot<PuntoEntity>()
        coEvery { puntoDao.insertar(capture(guardado)) } just Runs

        assertTrue(repo.guardarPunto(puntoNuevo()))

        coVerify { puntoApi.crear(match { it.id == null && it.codigoQR == "QR_NUEVO" }) }
        assertEquals(9, guardado.captured.id)
        assertFalse(guardado.captured.fueVisitado)
    }

    @Test
    fun guardarPunto_existente_actualizaYConservaLaVisitaLocal() = runTest {
        coEvery { puntoApi.actualizar(4L, any()) } returns puntoDto(4)
        coEvery { puntoDao.buscarPorId(4) } returns punto(4, visitado = true, selfie = "/s.jpg")
        val guardado = slot<PuntoEntity>()
        coEvery { puntoDao.insertar(capture(guardado)) } just Runs

        assertTrue(repo.guardarPunto(puntoNuevo(id = 4)))

        coVerify { puntoApi.actualizar(4L, any()) }
        assertTrue(guardado.captured.fueVisitado)
        assertEquals("/s.jpg", guardado.captured.selfieRuta)
    }

    @Test
    fun guardarPunto_conflicto409_devuelveFalse_yOtroErrorSeRelanza() = runTest {
        coEvery { puntoApi.crear(any()) } throws httpError(409)
        assertFalse(repo.guardarPunto(puntoNuevo()))
        coVerify(exactly = 0) { puntoDao.insertar(any()) }

        coEvery { puntoApi.crear(any()) } throws httpError(500)
        assertTrue(excepcionDe { repo.guardarPunto(puntoNuevo()) } is HttpException)
    }

    @Test
    fun eliminarPunto_borraRemotoLocalVisitasYSelfies() = runTest {
        val selfie = archivoTemporal()
        coEvery { visitaDao.selfiesDePunto(3) } returns listOf(selfie.absolutePath)

        repo.eliminarPunto(3)

        coVerify { puntoApi.eliminar(3L) }
        coVerify { puntoDao.eliminar(3) }
        coVerify { visitaDao.eliminarPorPunto(3) }
        assertFalse(selfie.exists())
    }

    @Test
    fun eliminarPunto_toleraNoEncontrado404_peroRelanzaOtrosErrores() = runTest {
        coEvery { puntoApi.eliminar(3L) } throws httpError(404)
        repo.eliminarPunto(3)
        coVerify(exactly = 1) { puntoDao.eliminar(3) }

        coEvery { puntoApi.eliminar(5L) } throws httpError(500)
        assertTrue(excepcionDe { repo.eliminarPunto(5) } is HttpException)
        coVerify(exactly = 0) { puntoDao.eliminar(5) }
    }

    // ---------- sincronización ----------

    @Test
    fun sincronizarPuntos_guardaRemotosConservaVisitasYEliminaLosSobrantes() = runTest {
        coEvery { puntoApi.listar() } returns listOf(puntoDto(1), puntoDto(2))
        coEvery { puntoDao.buscarPorId(1) } returns punto(1, visitado = true, selfie = "/s.jpg")
        puntosFlow.value = listOf(punto(1), punto(99, qr = "QR_VIEJO"))
        val guardados = mutableListOf<PuntoEntity>()
        coEvery { puntoDao.insertar(capture(guardados)) } just Runs

        repo.sincronizarPuntos()

        assertEquals(listOf(1, 2), guardados.map { it.id })
        assertTrue(guardados[0].fueVisitado)
        assertFalse(guardados[1].fueVisitado)
        coVerify { puntoDao.eliminar(99) }
        coVerify(exactly = 0) { puntoDao.eliminar(1) }
    }

    @Test
    fun sincronizarRanking_reemplazaElRankingLocal() = runTest {
        coEvery { jugadorApi.ranking() } returns listOf(
            EntradaRankingDto("Ana", 50), EntradaRankingDto("Luis", 20)
        )

        repo.sincronizarRanking()

        coVerify { rankingDao.borrarTodo() }
        coVerify {
            rankingDao.insertarTodos(match { l ->
                l.map { it.nombre to it.puntos } == listOf("Ana" to 50, "Luis" to 20)
            })
        }
    }

    // ---------- gestión de jugadores ----------

    @Test
    fun eliminarJugador_cliente_borraEnBackendRoomVisitasYFotos() = runTest {
        val foto = archivoTemporal()
        val selfie = archivoTemporal()
        coEvery { perfilDao.buscarPorId(2) } returns perfil(id = 2, foto = foto.absolutePath)
        coEvery { visitaDao.selfiesDeJugador(2) } returns listOf(selfie.absolutePath)

        repo.eliminarJugador(2)

        coVerify { jugadorApi.eliminar(2L) }
        coVerify { perfilDao.eliminar(2) }
        coVerify { visitaDao.eliminarPorJugador(2) }
        assertFalse(foto.exists())
        assertFalse(selfie.exists())
    }

    @Test
    fun eliminarJugador_cuentasProtegidasOInexistentes_noHaceNada() = runTest {
        repo.eliminarJugador(2) // no existe
        coEvery { perfilDao.buscarPorId(1) } returns perfil(id = 1, rol = Rol.ADMINISTRADOR)
        repo.eliminarJugador(1)

        coVerify(exactly = 0) { jugadorApi.eliminar(any()) }
        coVerify(exactly = 0) { perfilDao.eliminar(any()) }
    }

    @Test
    fun eliminarJugador_toleraNoEncontrado404_peroRelanzaOtrosErrores() = runTest {
        coEvery { perfilDao.buscarPorId(2) } returns perfil(id = 2)
        coEvery { jugadorApi.eliminar(2L) } throws httpError(404)
        repo.eliminarJugador(2)
        coVerify(exactly = 1) { perfilDao.eliminar(2) }

        coEvery { jugadorApi.eliminar(2L) } throws httpError(500)
        assertTrue(excepcionDe { repo.eliminarJugador(2) } is HttpException)
    }

    @Test
    fun actualizarJugador_validaAntesDeActualizar() = runTest {
        assertFalse(repo.actualizarJugador(2, "Eva", "e@d.cl")) // no existe

        coEvery { perfilDao.buscarPorId(1) } returns perfil(id = 1, rol = Rol.GENERADOR)
        assertFalse(repo.actualizarJugador(1, "X", "x@d.cl")) // no es cliente

        coEvery { perfilDao.buscarPorId(2) } returns perfil(id = 2)
        coEvery { perfilDao.contarPorEmail("e@d.cl", 2) } returns 1
        assertFalse(repo.actualizarJugador(2, "Eva", "e@d.cl")) // correo usado

        coVerify(exactly = 0) { jugadorApi.actualizar(any(), any()) }
    }

    @Test
    fun actualizarJugador_exito_actualizaBackendYRoom() = runTest {
        coEvery { perfilDao.buscarPorId(2) } returns perfil(id = 2)

        assertTrue(repo.actualizarJugador(2, " Eva ", " e@d.cl "))

        coVerify { jugadorApi.actualizar(2L, match { it.nombreUsuario == "Eva" && it.rol == "CLIENTE" }) }
        coVerify { perfilDao.actualizar(match { it.nombreUsuario == "Eva" && it.email == "e@d.cl" }) }
    }

    @Test
    fun actualizarJugador_conflicto409_devuelveFalse_yOtroErrorSeRelanza() = runTest {
        coEvery { perfilDao.buscarPorId(2) } returns perfil(id = 2)
        coEvery { jugadorApi.actualizar(any(), any()) } throws httpError(409)
        assertFalse(repo.actualizarJugador(2, "Eva", "e@d.cl"))
        coVerify(exactly = 0) { perfilDao.actualizar(any()) }

        coEvery { jugadorApi.actualizar(any(), any()) } throws httpError(500)
        assertTrue(excepcionDe { repo.actualizarJugador(2, "Eva", "e@d.cl") } is HttpException)
    }

    // ---------- perfil propio y selfies ----------

    @Test
    fun actualizarPerfil_sinSesionOSinPerfil_devuelveFalse() = runTest {
        assertFalse(repo.actualizarPerfil("Ana", "a@d.cl", null)) // sin sesión
        coEvery { sesionDao.sesionActual() } returns 1
        assertFalse(repo.actualizarPerfil("Ana", "a@d.cl", null)) // sin perfil
    }

    @Test
    fun actualizarPerfil_correoUsadoPorOtro_devuelveFalse() = runTest {
        coEvery { sesionDao.sesionActual() } returns 1
        coEvery { perfilDao.buscarPorId(1) } returns perfil()
        coEvery { perfilDao.contarPorEmail("x@d.cl", 1) } returns 1

        assertFalse(repo.actualizarPerfil("Ana", "x@d.cl", null))
        coVerify(exactly = 0) { jugadorApi.actualizar(any(), any()) }
    }

    @Test
    fun actualizarPerfil_conflicto409_devuelveFalse_yOtroErrorSeRelanza() = runTest {
        coEvery { sesionDao.sesionActual() } returns 1
        coEvery { perfilDao.buscarPorId(1) } returns perfil()
        coEvery { jugadorApi.actualizar(any(), any()) } throws httpError(409)
        assertFalse(repo.actualizarPerfil("Ana", "a@d.cl", null))

        coEvery { jugadorApi.actualizar(any(), any()) } throws httpError(500)
        assertTrue(excepcionDe { repo.actualizarPerfil("Ana", "a@d.cl", null) } is HttpException)
    }

    @Test
    fun actualizarPerfil_exito_guardaYBorraLaFotoAnterior() = runTest {
        val anterior = archivoTemporal()
        coEvery { sesionDao.sesionActual() } returns 1
        coEvery { perfilDao.buscarPorId(1) } returns perfil(foto = anterior.absolutePath)

        assertTrue(repo.actualizarPerfil(" Nuevo ", " n@d.cl ", "/nueva.jpg"))

        coVerify {
            perfilDao.actualizar(match {
                it.nombreUsuario == "Nuevo" && it.email == "n@d.cl" && it.fotoRuta == "/nueva.jpg"
            })
        }
        assertFalse(anterior.exists())
    }

    @Test
    fun guardarSelfie_sinSesion_noHaceNada() = runTest {
        repo.guardarSelfie(1, "/s.jpg")
        coVerify(exactly = 0) { visitaDao.guardarSelfie(any(), any(), any()) }
    }

    @Test
    fun guardarSelfie_reemplazaYBorraLaAnterior() = runTest {
        val anterior = archivoTemporal()
        coEvery { sesionDao.sesionActual() } returns 1
        coEvery { visitaDao.selfieActual(1, 5) } returns anterior.absolutePath

        repo.guardarSelfie(5, "/nueva.jpg")

        coVerify { visitaDao.guardarSelfie(1, 5, "/nueva.jpg") }
        coVerify { puntoDao.guardarSelfie(5, "/nueva.jpg") }
        assertFalse(anterior.exists())
    }

    // ---------- registrar visita ----------

    private fun conSesionYPunto() {
        coEvery { sesionDao.sesionActual() } returns 1
        coEvery { puntoDao.buscarPorQr("QR_LAMONEDA_01") } returns punto()
    }

    @Test
    fun registrarVisita_qrDesconocido_esInvalido() = runTest {
        coEvery { sesionDao.sesionActual() } returns 1
        assertEquals(ResultadoEscaneo.Invalido, repo.registrarVisita("NO_EXISTE", -33.4429, -70.6539))
    }

    @Test
    fun registrarVisita_sinSesion_esInvalido() = runTest {
        coEvery { puntoDao.buscarPorQr("QR_LAMONEDA_01") } returns punto()
        assertEquals(ResultadoEscaneo.Invalido, repo.registrarVisita("QR_LAMONEDA_01", -33.4429, -70.6539))
    }

    @Test
    fun registrarVisita_ubicacionInvalida_esSinUbicacion() = runTest {
        conSesionYPunto()
        val qr = "QR_LAMONEDA_01"
        assertEquals(ResultadoEscaneo.SinUbicacion, repo.registrarVisita(qr, null, -70.6))
        assertEquals(ResultadoEscaneo.SinUbicacion, repo.registrarVisita(qr, -33.4, null))
        assertEquals(ResultadoEscaneo.SinUbicacion, repo.registrarVisita(qr, Double.NaN, -70.6))
        assertEquals(ResultadoEscaneo.SinUbicacion, repo.registrarVisita(qr, 95.0, -70.6))
        assertEquals(ResultadoEscaneo.SinUbicacion, repo.registrarVisita(qr, -33.4, 200.0))
    }

    @Test
    fun registrarVisita_lejosDelPunto_esFueraDeRango() = runTest {
        conSesionYPunto()

        val r = repo.registrarVisita("QR_LAMONEDA_01", -33.45, -70.66)

        assertTrue(r is ResultadoEscaneo.FueraDeRango)
        assertTrue((r as ResultadoEscaneo.FueraDeRango).distanciaMetros > 50f)
        coVerify(exactly = 0) { visitaDao.insertarSiNoExiste(any()) }
    }

    @Test
    fun registrarVisita_puntoYaVisitado_esYaVisitado() = runTest {
        conSesionYPunto()
        coEvery { visitaDao.buscar(1, 1) } returns VisitaEntity(1, 1, "f")

        val r = repo.registrarVisita("QR_LAMONEDA_01", -33.4429, -70.6539)

        assertTrue(r is ResultadoEscaneo.YaVisitado)
        coVerify(exactly = 0) { jugadorApi.registrarVisita(any(), any()) }
    }

    @Test
    fun registrarVisita_exito_guardaVisitaYAvisaAlBackend() = runTest {
        conSesionYPunto()
        coEvery { jugadorApi.ranking() } returns listOf(EntradaRankingDto("Ana", 10))

        val r = repo.registrarVisita("QR_LAMONEDA_01", -33.4429, -70.6539, 560.0)

        assertTrue(r is ResultadoEscaneo.Exito)
        val p = (r as ResultadoEscaneo.Exito).punto
        assertTrue(p.fueVisitado)
        assertEquals(560.0, p.altitudVisita!!, 0.001)
        coVerify { visitaDao.insertarSiNoExiste(match { it.jugadorId == 1 && it.puntoId == 1 }) }
        coVerify { jugadorApi.registrarVisita(1L, VisitaRequestDto(1L)) }
        coVerify { rankingDao.borrarTodo() } // el ranking se sincroniza después de la visita
    }

    @Test
    fun registrarVisita_altitudNoFinita_seGuardaSinAltitud() = runTest {
        conSesionYPunto()

        val r = repo.registrarVisita("QR_LAMONEDA_01", -33.4429, -70.6539, Double.NaN)

        assertNull((r as ResultadoEscaneo.Exito).punto.altitudVisita)
    }

    @Test
    fun registrarVisita_backendCaido_laVisitaLocalSeMantiene() = runTest {
        conSesionYPunto()
        coEvery { jugadorApi.registrarVisita(any(), any()) } throws IOException("sin red")
        assertTrue(repo.registrarVisita("QR_LAMONEDA_01", -33.4429, -70.6539) is ResultadoEscaneo.Exito)

        coEvery { jugadorApi.registrarVisita(any(), any()) } throws httpError(503)
        assertTrue(repo.registrarVisita("QR_LAMONEDA_01", -33.4429, -70.6539) is ResultadoEscaneo.Exito)
    }

    @Test
    fun registrarVisita_insercionIgnorada_esYaVisitado() = runTest {
        conSesionYPunto()
        coEvery { visitaDao.insertarSiNoExiste(any()) } returns -1L

        val r = repo.registrarVisita("QR_LAMONEDA_01", -33.4429, -70.6539)

        assertTrue(r is ResultadoEscaneo.YaVisitado)
        coVerify(exactly = 0) { jugadorApi.registrarVisita(any(), any()) }
    }

    // ---------- registrar selfie ----------

    @Test
    fun registrarSelfie_visitaNueva_otorgaBonusYGuardaLaRuta() = runTest {
        conSesionYPunto()

        val (resultado, bonus) =
            repo.registrarSelfie("QR_LAMONEDA_01", -33.4429, -70.6539, null, "/f.jpg")

        assertTrue(resultado is ResultadoEscaneo.Exito)
        assertTrue(bonus)
        coVerify { visitaDao.guardarSelfie(1, 1, "/f.jpg") }
    }

    @Test
    fun registrarSelfie_yaTeniaSelfie_noOtorgaBonus() = runTest {
        conSesionYPunto()
        coEvery { visitaDao.selfieActual(1, 1) } returns "/vieja.jpg"

        val (_, bonus) = repo.registrarSelfie("QR_LAMONEDA_01", -33.4429, -70.6539, null, "/f.jpg")

        assertFalse(bonus)
        coVerify(exactly = 0) { visitaDao.guardarSelfie(any(), any(), any()) }
    }

    @Test
    fun registrarSelfie_fueraDeRango_noOtorgaBonus() = runTest {
        conSesionYPunto()

        val (resultado, bonus) = repo.registrarSelfie("QR_LAMONEDA_01", -33.45, -70.66, null, "/f.jpg")

        assertTrue(resultado is ResultadoEscaneo.FueraDeRango)
        assertFalse(bonus)
    }

    @Test
    fun registrarSelfie_puntoYaVisitadoSinSelfie_otorgaBonus() = runTest {
        conSesionYPunto()
        coEvery { visitaDao.buscar(1, 1) } returns VisitaEntity(1, 1, "f")

        val (resultado, bonus) = repo.registrarSelfie("QR_LAMONEDA_01", -33.4429, -70.6539, null, "/f.jpg")

        assertTrue(resultado is ResultadoEscaneo.YaVisitado)
        assertTrue(bonus)
    }
}
