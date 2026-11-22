package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprendoiasantiago.data.remote.EstadoRed
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.repository.PuntoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PuntosViewModel(
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
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PuntosUiState()
    )

    // NUEVO: estado de la conexión con el backend (cargando, error o ok) para avisar en pantalla
    val estadoRed: StateFlow<EstadoRed> = repositorio.estadoRed

    init {
        // importante este es el unico lugar donde se siembran los datos inciiales
        // puntos, perfil y ranking. No repetir en otro ViewModel
        // porque el ranking seria duplicado
        viewModelScope.launch {
            repositorio.sembrarDatosIniciales()
            // NUEVO: luego se sincroniza con el servidor; si no hay conexión queda lo local
            repositorio.sincronizar()
        }
    }

    // NUEVO: lo usa el botón "Reintentar" del banner
    fun reintentarSincronizacion() {
        viewModelScope.launch { repositorio.sincronizar() }
    }

    fun cambiarFiltro(nuevoFiltro: FiltroPuntos) {
        filtro.value = nuevoFiltro
    }

    fun guardarSelfie(puntoId: Int, ruta: String) {
        viewModelScope.launch { repositorio.guardarSelfie(puntoId, ruta) }
    }

    // --- Gestión de puntos y cápsulas (Administrador / Creador de Contenido) ---

    /** Crea (id = 0) o actualiza un punto. onResultado(false) = código QR repetido. */
    fun guardarPunto(punto: PuntoInteres, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch { onResultado(repositorio.guardarPunto(punto)) }
    }

    fun eliminarPunto(id: Int) {
        viewModelScope.launch { repositorio.eliminarPunto(id) }
    }
}