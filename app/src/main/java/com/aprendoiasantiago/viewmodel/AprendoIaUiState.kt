package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.PuntoInteres

/** Opciones de los FilterChips de la lista. */
enum class FiltroPuntos(val etiqueta: String) {
    TODOS("Todos"),
    PENDIENTES("Pendientes"),
    VISITADOS("Visitados")
}

/**
 * Estado completo de la UI en un único objeto inmutable.
 * Los valores derivados (lista filtrada, ranking ordenado) se calculan
 * aquí, así nunca quedan desincronizados con los datos base.
 */
data class AprendoIaUiState(
    val perfil: PerfilUsuario,
    val puntos: List<PuntoInteres> = emptyList(),
    val rankingBase: List<EntradaRanking> = emptyList(),
    val filtro: FiltroPuntos = FiltroPuntos.TODOS
) {
    val puntosFiltrados: List<PuntoInteres>
        get() = when (filtro) {
            FiltroPuntos.TODOS -> puntos
            FiltroPuntos.PENDIENTES -> puntos.filter { !it.fueVisitado }
            FiltroPuntos.VISITADOS -> puntos.filter { it.fueVisitado }
        }

    /** Ranking de otros estudiantes + el usuario actual, ordenado por puntos. */
    val ranking: List<EntradaRanking>
        get() = (rankingBase + EntradaRanking(perfil.nombreUsuario, perfil.puntosTotales, true))
            .sortedByDescending { it.puntos }
}