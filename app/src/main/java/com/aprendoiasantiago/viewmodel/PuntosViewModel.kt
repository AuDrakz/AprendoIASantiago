package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.repository.PuntoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
import retrofit2.HttpException

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

    init {
        viewModelScope.launch {
            repositorio.sembrarDatosIniciales()
            try {
                repositorio.sincronizarPuntos()
                repositorio.sincronizarRanking()
            } catch (_: Exception) {
            }
        }
    }

    fun cambiarFiltro(nuevoFiltro: FiltroPuntos) {
        filtro.value = nuevoFiltro
    }

    fun guardarSelfie(puntoId: Int, ruta: String) {
        viewModelScope.launch { repositorio.guardarSelfie(puntoId, ruta) }
    }

    // --- Gestión de puntos y cápsulas (Administrador / Creador de Contenido) ---

    /** Crea (id = 0) o actualiza un punto. onResultado(false) = no se pudo guardar. */
    fun guardarPunto(punto: PuntoInteres, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = try {
                repositorio.guardarPunto(punto)
            } catch (e: Exception) {
                val detalle = (e as? HttpException)?.response()?.errorBody()?.string()
                Log.e("PuntosVM", "Error al guardar punto: $detalle | url=[${punto.urlVideo}]", e)
                false
            }
            onResultado(ok)
        }
    }

    fun eliminarPunto(id: Int) {
        viewModelScope.launch {
            try {
                repositorio.eliminarPunto(id)
            } catch (e: Exception) {
                Log.e("PuntosVM", "Error al eliminar punto", e)
            }
        }
    }
}