@file:OptIn(ExperimentalCoroutinesApi::class)

package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.MainDispatcherRule
import com.aprendoiasantiago.httpError
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.recolectar
import com.aprendoiasantiago.repositorioFalso
import com.aprendoiasantiago.util.ErrorRed
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.just
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class PuntosViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun punto(id: Int, visitado: Boolean) = PuntoInteres(
        id = id, titulo = "P$id", conceptoIA = "IA", latitud = -33.4, longitud = -70.6,
        codigoQR = "QR_$id", puntosRecompensa = 10, urlVideo = "u", fueVisitado = visitado
    )

    @Test
    fun init_siembraYSincronizaPuntosYRanking() = runTest {
        val repo = repositorioFalso()

        PuntosViewModel(repo)

        coVerifyOrder {
            repo.sembrarDatosIniciales()
            repo.sincronizarPuntos()
            repo.sincronizarRanking()
        }
    }

    @Test
    fun init_siLaSincronizacionFalla_publicaElErrorSinCaerse() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.sincronizarPuntos() } throws IOException("sin red")

        val vm = PuntosViewModel(repo)

        assertEquals(ErrorRed.SIN_CONEXION, vm.red.error.value)
        coVerify(exactly = 0) { repo.sincronizarRanking() }
    }

    @Test
    fun uiState_publicaLosPuntosYAplicaElFiltro() = runTest {
        val repo = repositorioFalso(puntos = listOf(punto(1, true), punto(2, false), punto(3, false)))
        val vm = PuntosViewModel(repo)
        recolectar(vm.uiState)

        assertEquals(FiltroPuntos.TODOS, vm.uiState.value.filtro)
        assertEquals(3, vm.uiState.value.puntosFiltrados.size)

        vm.cambiarFiltro(FiltroPuntos.PENDIENTES)
        assertEquals(listOf(2, 3), vm.uiState.value.puntosFiltrados.map { it.id })

        vm.cambiarFiltro(FiltroPuntos.VISITADOS)
        assertEquals(listOf(1), vm.uiState.value.puntosFiltrados.map { it.id })
    }

    @Test
    fun guardarSelfie_llamaAlRepositorio() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.guardarSelfie(2, "/s.jpg") } just Runs
        val vm = PuntosViewModel(repo)

        vm.guardarSelfie(2, "/s.jpg")

        coVerify(exactly = 1) { repo.guardarSelfie(2, "/s.jpg") }
    }

    @Test
    fun guardarPunto_entregaElResultadoDelRepositorio() = runTest {
        val repo = repositorioFalso()
        val nuevo = punto(0, false)
        coEvery { repo.guardarPunto(nuevo) } returns true
        coEvery { repo.guardarPunto(nuevo.copy(codigoQR = "REPETIDO")) } returns false
        val vm = PuntosViewModel(repo)

        val resultados = mutableListOf<Boolean>()
        vm.guardarPunto(nuevo) { resultados += it }
        vm.guardarPunto(nuevo.copy(codigoQR = "REPETIDO")) { resultados += it }

        assertEquals(listOf(true, false), resultados)
        assertNull(vm.red.error.value)
    }

    @Test
    fun guardarPunto_conErrorDeRed_noLlamaAlCallbackYPublicaElError() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.guardarPunto(any()) } throws httpError(400)
        val vm = PuntosViewModel(repo)
        var llamado = false

        vm.guardarPunto(punto(0, false)) { llamado = true }

        assertFalse(llamado)
        assertEquals(ErrorRed.DATOS_INVALIDOS, vm.red.error.value)
    }

    @Test
    fun eliminarPunto_exito_llamaAlRepositorio_yConErrorPublicaElError() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.eliminarPunto(1) } just Runs
        coEvery { repo.eliminarPunto(2) } throws httpError(404)
        val vm = PuntosViewModel(repo)

        vm.eliminarPunto(1)
        assertNull(vm.red.error.value)

        vm.eliminarPunto(2)
        assertEquals(ErrorRed.NO_ENCONTRADO, vm.red.error.value)
        assertFalse(vm.red.cargando.value)

        vm.red.limpiarError()
        assertNull(vm.red.error.value)
    }
}
