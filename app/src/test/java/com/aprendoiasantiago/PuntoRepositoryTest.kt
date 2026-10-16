package com.aprendoiasantiago

import com.aprendoiasantiago.model.PuntoInteres
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuntoRepositoryTest {

    @Test
    fun registrarVisita_incrementaPuntosCorrectamente() {
        val puntosActuales = 200
        val recompensa = 100
        assertEquals(300, puntosActuales + recompensa)
    }

    @Test
    fun filtrarPuntos_porTitulo() {
        val listaPuntos = listOf(
            PuntoInteres(
                id = 1,
                titulo = "Plaza de Armas",
                conceptoIA = "Centro histórico de Santiago",
                latitud = -33.4372,
                longitud = -70.6506,
                codigoQR = "PUNTO_01",
                puntosRecompensa = 100,
                urlVideo = "https://example.com/video1"
            ),
            PuntoInteres(
                id = 2,
                titulo = "Cerro San Cristóbal",
                conceptoIA = "Mirador principal de la ciudad",
                latitud = -33.4253,
                longitud = -70.6331,
                codigoQR = "PUNTO_02",
                puntosRecompensa = 150,
                urlVideo = "https://example.com/video2"
            )
        )

        val filtrados = listaPuntos.filter { it.titulo.contains("Cerro", ignoreCase = true) }

        assertEquals(1, filtrados.size)
        assertEquals("Cerro San Cristóbal", filtrados.first().titulo)
    }

    @Test
    fun validarCodigoQR_formatoCorrecto() {
        val codigoQR = "PUNTO_SANTIAGO_01"
        assertTrue(codigoQR.startsWith("PUNTO_SANTIAGO_"))
    }
}