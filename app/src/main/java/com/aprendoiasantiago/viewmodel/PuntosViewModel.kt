package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprendoiasantiago.repository.PuntoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PuntosViewModel (
    private val repositorio: PuntoRepository
) : ViewModel() {
    // unico que vive solo en memoria: el filtro elegido en pantalla
    private val filtro = MutableStateFlow(FiltroPuntos.TODOS)

    val uiState: StateFlow<PuntosUiState> = combine(
        repositorio.puntos,
        filtro
    ) { puntos, filtroActual ->
        PuntosUiState(puntos = puntos, filtro = filtroActual)
    }.stateIn(
        scope = viewModelScope ,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PuntosUiState()
    )

    init {
        // importante este es el unico lugar donde se siembran los datos inciiales
        // puntos, perfil y ranking. No repetir en otro ViewModel
        // porque el ranking seria duplicado
        viewModelScope.launch { repositorio.sembrarDatosIniciales() }
    }

    fun cambiarFiltro(nuevoFiltro: FiltroPuntos) {
        filtro.value = nuevoFiltro
    }

    fun guardarSelfie(puntoId: Int, ruta: String) {
        viewModelScope.launch { repositorio.guardarSelfie(puntoId, ruta) }
    }
}