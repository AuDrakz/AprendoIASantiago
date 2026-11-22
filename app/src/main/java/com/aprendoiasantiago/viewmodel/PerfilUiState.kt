package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario

/** Estado de la pantallas de perfil, edicion y Ranking */
data class PerfilUiState(
    val perfil: PerfilUsuario = PerfilUsuario(id = 0, nombreUsuario = "Cargando...", email =""),
    val rankingBase: List<EntradaRanking> = emptyList()
) {
    /** Ranking de otros estudiantes + usuario acutal, ordenado por puntos */
    val ranking: List<EntradaRanking>
        get() = (
                // NUEVO: el servidor ya incluye al jugador actual; se quita para no mostrarlo dos veces
                rankingBase.filterNot { it.nombre.equals(perfil.nombreUsuario, ignoreCase = true) } +
                        EntradaRanking(perfil.nombreUsuario, perfil.puntosTotales, true)
                ).sortedByDescending { it.puntos }
}