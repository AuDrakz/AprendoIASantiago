@file:OptIn(ExperimentalCoroutinesApi::class)

package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.MainDispatcherRule
import com.aprendoiasantiago.R
import com.aprendoiasantiago.httpError
import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.recolectar
import com.aprendoiasantiago.repositorioFalso
import com.aprendoiasantiago.util.ErrorRed
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

class PerfilViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val ana = PerfilUsuario(id = 1, nombreUsuario = "Ana", email = "ana@duoc.cl", puntosTotales = 200)

    @Test
    fun uiState_combinaPerfilConRankingOrdenadoPorPuntos() = runTest {
        val repo = repositorioFalso(
            perfil = ana,
            ranking = listOf(EntradaRanking("Luis", 300), EntradaRanking("Eva", 100))
        )
        val vm = PerfilViewModel(repo)

        recolectar(vm.uiState)

        val ranking = vm.uiState.value.ranking
        assertEquals(listOf("Luis", "Ana", "Eva"), ranking.map { it.nombre })
        assertTrue(ranking[1].esUsuarioActual)
    }

    @Test
    fun cargarFormulario_ignoraPerfilSinCargarYSoloCargaUnaVez() {
        val vm = PerfilViewModel(repositorioFalso())

        vm.cargarFormulario(PerfilUsuario(id = 0, nombreUsuario = "Cargando...", email = ""))
        assertEquals("", vm.formulario.value.nombre)

        vm.cargarFormulario(ana)
        vm.cargarFormulario(ana.copy(nombreUsuario = "Otro"))
        assertEquals("Ana", vm.formulario.value.nombre)
        assertEquals("ana@duoc.cl", vm.formulario.value.email)

        vm.cerrarFormulario()
        vm.cargarFormulario(ana.copy(nombreUsuario = "Otro"))
        assertEquals("Otro", vm.formulario.value.nombre)
    }

    @Test
    fun camposDelFormulario_validanAlEscribir() {
        val vm = PerfilViewModel(repositorioFalso())

        vm.onNombreCambia("")
        vm.onEmailCambia("malo")

        assertEquals(R.string.error_nombre_obligatorio, vm.formulario.value.errorNombre)
        assertEquals(R.string.error_correo_invalido, vm.formulario.value.errorEmail)

        vm.onNombreCambia("Ana")
        vm.onEmailCambia("ana@duoc.cl")
        assertNull(vm.formulario.value.errorNombre)
        assertNull(vm.formulario.value.errorEmail)
    }

    @Test
    fun onFotoCambia_borraElBorradorAnterior() {
        val borrador = File.createTempFile("borrador", ".jpg")
        val vm = PerfilViewModel(repositorioFalso())
        vm.cargarFormulario(ana.copy(fotoRuta = borrador.absolutePath))

        vm.onFotoCambia("/nueva.jpg")

        assertFalse(borrador.exists())
        assertEquals("/nueva.jpg", vm.formulario.value.fotoRuta)
    }

    @Test
    fun onFotoCambia_noBorraLaFotoYaGuardadaEnElPerfil() = runTest {
        val guardada = File.createTempFile("guardada", ".jpg")
        val perfil = ana.copy(fotoRuta = guardada.absolutePath)
        val vm = PerfilViewModel(repositorioFalso(perfil = perfil))
        recolectar(vm.uiState)
        vm.cargarFormulario(perfil)

        vm.onFotoCambia(null)

        assertTrue(guardada.exists())
        assertNull(vm.formulario.value.fotoRuta)
        guardada.delete()
    }

    @Test
    fun validarFormulario_conCamposInvalidos_devuelveFalse() {
        val vm = PerfilViewModel(repositorioFalso())

        assertFalse(vm.validarFormulario())

        assertEquals(R.string.error_nombre_obligatorio, vm.formulario.value.errorNombre)
        assertEquals(R.string.error_correo_obligatorio, vm.formulario.value.errorEmail)
    }

    @Test
    fun guardarFormulario_invalido_noLlamaAlRepositorioNiAlCallback() = runTest {
        val repo = repositorioFalso()
        val vm = PerfilViewModel(repo)
        var llamado = false

        vm.guardarFormulario { llamado = true }

        assertFalse(llamado)
        coVerify(exactly = 0) { repo.actualizarPerfil(any(), any(), any()) }
    }

    @Test
    fun guardarFormulario_valido_guardaYAvisa() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.actualizarPerfil("Ana", "ana@duoc.cl", null) } returns true
        val vm = PerfilViewModel(repo)
        vm.cargarFormulario(ana)
        var llamado = false

        vm.guardarFormulario { llamado = true }

        assertTrue(llamado)
        coVerify(exactly = 1) { repo.actualizarPerfil("Ana", "ana@duoc.cl", null) }
    }

    @Test
    fun guardarFormulario_correoDuplicado_publicaElErrorYNoAvisa() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.actualizarPerfil(any(), any(), any()) } returns false
        val vm = PerfilViewModel(repo)
        vm.cargarFormulario(ana)
        var llamado = false

        vm.guardarFormulario { llamado = true }

        assertFalse(llamado)
        assertEquals(R.string.error_correo_duplicado, vm.formulario.value.errorEmail)
    }

    @Test
    fun guardarFormulario_conErrorDeRed_publicaElErrorYNoAvisa() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.actualizarPerfil(any(), any(), any()) } throws httpError(409)
        val vm = PerfilViewModel(repo)
        vm.cargarFormulario(ana)
        var llamado = false

        vm.guardarFormulario { llamado = true }

        assertFalse(llamado)
        assertEquals(ErrorRed.CONFLICTO, vm.red.error.value)
    }
}
