@file:OptIn(ExperimentalCoroutinesApi::class)

package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.MainDispatcherRule
import com.aprendoiasantiago.R
import com.aprendoiasantiago.httpError
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.Rol
import com.aprendoiasantiago.recolectar
import com.aprendoiasantiago.repositorioFalso
import com.aprendoiasantiago.repository.ResultadoRegistro
import com.aprendoiasantiago.util.ErrorRed
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SesionViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val admin = PerfilUsuario(id = 5, nombreUsuario = "Admin", email = "a@d.cl", rol = Rol.ADMINISTRADOR)

    private fun SesionViewModel.llenarRegistroValido() {
        onNombreCambia("Ana")
        onEmailCambia("ana@duoc.cl")
        onContrasenaCambia("1234")
        onAvatarCambia(2)
    }

    // ---------- estado ----------

    @Test
    fun estado_empiezaCargandoYSinJugadorPasaASinSesion() = runTest {
        val repo = repositorioFalso()
        every { repo.jugadorActivo } returns flowOf<PerfilUsuario?>(null)
        val vm = SesionViewModel(repo)
        assertEquals(EstadoSesion.Cargando, vm.estado.value)

        recolectar(vm.estado)

        assertEquals(EstadoSesion.SinSesion, vm.estado.value)
    }

    @Test
    fun estado_conJugadorActivo_entregaIdYRol() = runTest {
        val repo = repositorioFalso(perfil = admin)
        val vm = SesionViewModel(repo)

        recolectar(vm.estado)

        assertEquals(EstadoSesion.ConSesion(5, Rol.ADMINISTRADOR), vm.estado.value)
    }

    @Test
    fun jugadores_publicaLaListaDelRepositorio() = runTest {
        val vm = SesionViewModel(repositorioFalso(perfil = admin))

        recolectar(vm.jugadores)

        assertEquals(listOf(admin), vm.jugadores.value)
    }

    // ---------- login / logout ----------

    @Test
    fun iniciarSesion_entregaElResultadoDelRepositorio() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.iniciarSesion(1, "123") } returns true
        coEvery { repo.iniciarSesion(1, "mala") } returns false
        val vm = SesionViewModel(repo)

        var ok: Boolean? = null
        vm.iniciarSesion(1, "123") { ok = it }
        assertEquals(true, ok)
        vm.iniciarSesion(1, "mala") { ok = it }
        assertEquals(false, ok)
    }

    @Test
    fun iniciarSesionConCredencial_entregaElResultadoDelRepositorio() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.iniciarSesionConCredencial("ana", "1234") } returns true
        val vm = SesionViewModel(repo)

        var ok: Boolean? = null
        vm.iniciarSesionConCredencial("ana", "1234") { ok = it }

        assertEquals(true, ok)
    }

    @Test
    fun cerrarSesion_llamaAlRepositorio() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.cerrarSesion() } returns Unit
        val vm = SesionViewModel(repo)

        vm.cerrarSesion()

        coVerify(exactly = 1) { repo.cerrarSesion() }
    }

    // ---------- gestión de jugadores ----------

    @Test
    fun eliminarJugador_exito_llamaAlRepositorioSinError() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.eliminarJugador(3) } just Runs
        val vm = SesionViewModel(repo)

        vm.eliminarJugador(3)

        coVerify { repo.eliminarJugador(3) }
        assertNull(vm.red.error.value)
        assertFalse(vm.red.cargando.value)
    }

    @Test
    fun eliminarJugador_conErrorHttp_publicaElErrorDeRed() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.eliminarJugador(3) } throws httpError(503)
        val vm = SesionViewModel(repo)

        vm.eliminarJugador(3)

        assertEquals(ErrorRed.SERVICIO_NO_DISPONIBLE, vm.red.error.value)
    }

    @Test
    fun actualizarJugador_entregaElResultado_ySiFallaLaRedNoLlamaAlCallback() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.actualizarJugador(2, "Eva", "e@d.cl") } returns true
        coEvery { repo.actualizarJugador(2, "Eva", "dup@d.cl") } returns false
        coEvery { repo.actualizarJugador(2, "Eva", "roto@d.cl") } throws IOException("sin red")
        val vm = SesionViewModel(repo)

        val recibidos = mutableListOf<Boolean>()
        vm.actualizarJugador(2, "Eva", "e@d.cl") { recibidos += it }
        vm.actualizarJugador(2, "Eva", "dup@d.cl") { recibidos += it }
        vm.actualizarJugador(2, "Eva", "roto@d.cl") { recibidos += it }

        assertEquals(listOf(true, false), recibidos)
        assertEquals(ErrorRed.SIN_CONEXION, vm.red.error.value)
    }

    // ---------- formulario de registro ----------

    @Test
    fun camposDelRegistro_validanAlEscribir() {
        val vm = SesionViewModel(repositorioFalso())

        vm.onNombreCambia("ab")
        vm.onEmailCambia("malo")
        vm.onTelefonoCambia("123")
        vm.onContrasenaCambia("12")
        vm.onDireccionCambia("Calle 1")
        vm.onAvatarCambia(4)

        val r = vm.registro.value
        assertEquals(R.string.error_nombre_min, r.errorNombre)
        assertEquals(R.string.error_correo_invalido, r.errorEmail)
        assertEquals(R.string.error_telefono_invalido, r.errorTelefono)
        assertEquals(R.string.error_contrasena_min, r.errorContrasena)
        assertEquals("Calle 1", r.direccion)
        assertEquals(4, r.avatar)

        vm.limpiarRegistro()
        assertEquals(RegistroUiState(), vm.registro.value)
    }

    @Test
    fun validarRegistro_conCamposVacios_publicaErroresYDevuelveFalse() {
        val vm = SesionViewModel(repositorioFalso())

        assertFalse(vm.validarRegistro())

        val r = vm.registro.value
        assertEquals(R.string.error_nombre_obligatorio, r.errorNombre)
        assertEquals(R.string.error_correo_obligatorio, r.errorEmail)
        assertEquals(R.string.error_contrasena_obligatoria, r.errorContrasena)
    }

    @Test
    fun registrar_conDatosInvalidos_noLlamaAlRepositorio() = runTest {
        val repo = repositorioFalso()
        val vm = SesionViewModel(repo)

        vm.registrar()

        coVerify(exactly = 0) { repo.registrarJugador(any(), any(), any(), any()) }
        assertFalse(vm.registro.value.guardando)
    }

    @Test
    fun registrar_exito_creaElJugadorYLimpiaElFormulario() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.registrarJugador("Ana", "ana@duoc.cl", 2, "1234") } returns ResultadoRegistro.EXITO
        val vm = SesionViewModel(repo)
        vm.llenarRegistroValido()

        vm.registrar()

        coVerify(exactly = 1) { repo.registrarJugador("Ana", "ana@duoc.cl", 2, "1234") }
        assertEquals(RegistroUiState(), vm.registro.value)
    }

    @Test
    fun registrar_correoDuplicado_publicaElErrorEnElCampo() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.registrarJugador(any(), any(), any(), any()) } returns ResultadoRegistro.CORREO_DUPLICADO
        val vm = SesionViewModel(repo)
        vm.llenarRegistroValido()

        vm.registrar()

        assertEquals(R.string.error_correo_duplicado, vm.registro.value.errorEmail)
        assertFalse(vm.registro.value.guardando)
    }

    @Test
    fun registrar_nombreDuplicado_publicaElErrorEnElCampo() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.registrarJugador(any(), any(), any(), any()) } returns ResultadoRegistro.NOMBRE_DUPLICADO
        val vm = SesionViewModel(repo)
        vm.llenarRegistroValido()

        vm.registrar()

        assertEquals(R.string.error_nombre_duplicado, vm.registro.value.errorNombre)
        assertFalse(vm.registro.value.guardando)
    }

    @Test
    fun registrar_conErrorDeRed_publicaElErrorYLiberaElBoton() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.registrarJugador(any(), any(), any(), any()) } throws IOException("sin red")
        val vm = SesionViewModel(repo)
        vm.llenarRegistroValido()

        vm.registrar()

        assertEquals(ErrorRed.SIN_CONEXION, vm.red.error.value)
        assertFalse(vm.registro.value.guardando)
        assertTrue(vm.registro.value.nombre == "Ana") // el formulario conserva lo escrito
    }
}
