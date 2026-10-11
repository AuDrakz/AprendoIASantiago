package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.model.PuntoInteres
import androidx.annotation.StringRes
import com.aprendoiasantiago.R

/** Opciones de los FilterChips de la lista. */
enum class FiltroPuntos(@param:StringRes val etiqueta: Int) {
    TODOS(R.string.filtro_todos),
    PENDIENTES(R.string.filtro_pendientes),
    VISITADOS(R.string.filtro_visitados)
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