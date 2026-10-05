package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.repository.PuntoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.aprendoiasantiago.model.ResultadoEscaneo

class AprendoIaViewModel(
    private val repositorio: PuntoRepository
) : ViewModel() {

    // Lo único que vive solo en memoria: el filtro elegido en pantalla
    private val filtro = MutableStateFlow(FiltroPuntos.TODOS)

    // Datos de prueba hasta que lleguen desde la API (Fase 6)
    private val rankingBase = listOf(
        EntradaRanking("Camila R.", 450),
        EntradaRanking("Matías P.", 300),
        EntradaRanking("Valentina S.", 150)
    )

    /**
     * Junta perfil + puntos (desde Room) + filtro en un solo estado.
     * Cuando Room cambia, este estado se recalcula solo.
     */
    val uiState: StateFlow<AprendoIaUiState> = combine(
        repositorio.perfil,
        repositorio.puntos,
        filtro
    ) { perfil, puntos, filtroActual ->
        AprendoIaUiState(
            perfil = perfil,
            puntos = puntos,
            rankingBase = rankingBase,
            filtro = filtroActual
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        // Estado inicial mientras Room responde (dura milisegundos)
        initialValue = AprendoIaUiState(
            perfil = PerfilUsuario(id = 0, nombreUsuario = "Cargando...", email = "")
        )
    )

    init {
        // Si es la primera vez, deja los datos iniciales en la base de datos
        viewModelScope.launch { repositorio.sembrarDatosIniciales() }
    }

    fun cambiarFiltro(nuevoFiltro: FiltroPuntos) {
        filtro.value = nuevoFiltro
    }

    /**
     * Procesa un QR leído. Corre en una corrutina porque Room no permite
     * acceder a la base de datos desde el hilo principal.
     * El resultado llega por el callback.
     */
    fun escanearCodigoQR(
        codigoLeido: String,
        onResultado: (ResultadoEscaneo) -> Unit = {}
    ) {
        viewModelScope.launch {
            onResultado(repositorio.registrarVisita(codigoLeido))
        }
    }

    /** Permite crear el ViewModel pasándole el repositorio. */
    class Factory(private val repositorio: PuntoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AprendoIaViewModel(repositorio) as T
    }
}