@file:OptIn(ExperimentalCoroutinesApi::class)

package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.MainDispatcherRule
import com.aprendoiasantiago.model.ResultadoEscaneo
import com.aprendoiasantiago.repositorioFalso
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EscanerViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun escanearCodigoQR_siembraRegistraLaVisitaYEntregaElResultado() = runTest {
        val repo = repositorioFalso()
        coEvery {
            repo.registrarVisita("QR_PLAZA_02", -33.43, -70.65, 540.0)
        } returns ResultadoEscaneo.SinUbicacion
        val vm = EscanerViewModel(repo)

        var recibido: ResultadoEscaneo? = null
        vm.escanearCodigoQR("QR_PLAZA_02", -33.43, -70.65, { recibido = it }, 540.0)

        assertEquals(ResultadoEscaneo.SinUbicacion, recibido)
        coVerifyOrder {
            repo.sembrarDatosIniciales()
            repo.registrarVisita("QR_PLAZA_02", -33.43, -70.65, 540.0)
        }
    }

    @Test
    fun escanearCodigoQR_sinCallbackNiAltitud_usaLosValoresPorDefecto() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.registrarVisita("X", null, null, null) } returns ResultadoEscaneo.Invalido
        val vm = EscanerViewModel(repo)

        vm.escanearCodigoQR("X", null, null)

        coVerify(exactly = 1) { repo.registrarVisita("X", null, null, null) }
    }

    @Test
    fun registrarSelfie_entregaElResultadoYElBonus() = runTest {
        val repo = repositorioFalso()
        coEvery {
            repo.registrarSelfie("QR_FORESTAL_03", -33.435, -70.642, null, "/f.jpg")
        } returns (ResultadoEscaneo.Invalido to true)
        val vm = EscanerViewModel(repo)

        var resultado: ResultadoEscaneo? = null
        var bonus = false
        vm.registrarSelfie("QR_FORESTAL_03", -33.435, -70.642, null, "/f.jpg") { r, b ->
            resultado = r
            bonus = b
        }

        assertTrue(resultado is ResultadoEscaneo.Invalido)
        assertTrue(bonus)
        coVerify { repo.sembrarDatosIniciales() }
    }

    @Test
    fun registrarSelfie_sinBonus_entregaFalse() = runTest {
        val repo = repositorioFalso()
        coEvery { repo.registrarSelfie(any(), any(), any(), any(), any()) } returns
            (ResultadoEscaneo.SinUbicacion to false)
        val vm = EscanerViewModel(repo)
        var bonus = true

        vm.registrarSelfie("X", null, null, null, "/f.jpg") { _, b -> bonus = b }

        assertFalse(bonus)
    }
}
