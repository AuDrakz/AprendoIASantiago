package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprendoiasantiago.repository.PuntoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PerfilViewModel (
    private val repositorio: PuntoRepository
) : ViewModel() {

    val uiState: StateFlow<PerfilUiState> = combine(
        repositorio.perfil,
        repositorio.rankingBase
    ) { perfil, rankingBase ->
        PerfilUiState(perfil = perfil, rankingBase = rankingBase)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PerfilUiState()
    )

    fun actualizarPerfil(nombreUsuario: String, email: String) {
        viewModelScope.launch {
            repositorio.actualizarPerfil(nombreUsuario,email)
        }
    }
}