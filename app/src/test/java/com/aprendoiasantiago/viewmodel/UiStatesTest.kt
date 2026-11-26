package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.PuntoInteres
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStatesTest {

    private fun punto(id: Int, visitado: Boolean) = PuntoInteres(
        id = id, titulo = "P$id", conceptoIA = "IA", latitud = 0.0, longitud = 0.0,
        codigoQR = "QR$id", puntosRecompensa = 10, urlVideo = "u", fueVisitado = visitado
    )

    @Test
    fun puntosUiState_filtraSegunElFiltroElegido() {
        val lista = listOf(punto(1, true), punto(2, false))

        assertEquals(2, PuntosUiState(lista, FiltroPuntos.TODOS).puntosFiltrados.size)
        assertEquals(listOf(2), PuntosUiState(lista, FiltroPuntos.PENDIENTES).puntosFiltrados.map { it.id })
        assertEquals(listOf(1), PuntosUiState(lista, FiltroPuntos.VISITADOS).puntosFiltrados.map { it.id })
    }

    @Test
    fun perfilUiState_ranking_incluyeAlUsuarioActualOrdenadoPorPuntos() {
        val estado = PerfilUiState(
            perfil = PerfilUsuario(id = 1, nombreUsuario = "Ana", email = "a@d.cl", puntosTotales = 50),
            rankingBase = listOf(EntradaRanking("Luis", 10), EntradaRanking("Eva", 90))
        )

        assertEquals(listOf("Eva", "Ana", "Luis"), estado.ranking.map { it.nombre })
        assertTrue(estado.ranking[1].esUsuarioActual)
    }

    @Test
    fun perfilUiState_porDefecto_muestraCargando() {
        assertEquals("Cargando...", PerfilUiState().perfil.nombreUsuario)
    }
}
