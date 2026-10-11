package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.model.PuntoInteres

/** Opciones de los FilterChips de la lista. */
enum class FiltroPuntos(val etiqueta: String) {
    TODOS("Todos"),
    PENDIENTES("Pendientes"),
    VISITADOS("Visitados")
}

/** Estado de las pantallas que muestran puntos (lista, mapa y detalles) */
data class PuntosUiState(
    val puntos: List<PuntoInteres> = emptyList(),
    val filtro: FiltroPuntos = FiltroPuntos.TODOS
) {
    val puntosFiltrados: List<PuntoInteres>
        get() = when (filtro) {
            FiltroPuntos.TODOS -> puntos
            FiltroPuntos.PENDIENTES -> puntos.filter { !it.fueVisitado }
            FiltroPuntos.VISITADOS -> puntos.filter { it.fueVisitado }
        }
}