package com.aprendoiasantiago.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprendoiasantiago.R
import com.aprendoiasantiago.repository.PuntoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class EscaneoUiState {
    object Idle : EscaneoUiState()
    object Cargando : EscaneoUiState()
    data class Exito(val puntoId: String) : EscaneoUiState()
    data class Error(val mensajeResId: Int) : EscaneoUiState()
}

class EscanerViewModel(
    private val repository: PuntoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EscaneoUiState>(EscaneoUiState.Idle)
    val uiState: StateFlow<EscaneoUiState> = _uiState

    // 1. Análisis del QR ejecutado en HILO SECUNDARIO (Dispatchers.Default)
    fun procesarCodigoQr(qrContent: String, userLocation: Location?) {
        viewModelScope.launch {
            _uiState.value = EscaneoUiState.Cargando

            // Pasar el análisis pesado fuera del Main Thread
            val resultado = withContext(Dispatchers.Default) {
                validarDistanciaYRegistrar(qrContent, userLocation)
            }
            _uiState.value = resultado
        }
    }

    private suspend fun validarDistanciaYRegistrar(qrContent: String, userLocation: Location?): EscaneoUiState {
        if (userLocation == null) return EscaneoUiState.Error(R.string.error_too_far)

        val punto = repository.obtenerPuntoPorId(qrContent)
            ?: return EscaneoUiState.Error(R.string.error_too_far)

        val locPunto = Location("").apply {
            latitude = punto.latitud
            longitude = punto.longitud
        }

        val distanciaMeters = userLocation.distanceTo(locPunto)

        // Validar que el usuario esté a menos de 50m del punto
        return if (distanciaMeters <= 50.0) {
            // Guardar altitud y fecha actual
            repository.marcarComoVisitado(
                idPunto = punto.id,
                altitud = userLocation.altitude,
                fecha = System.currentTimeMillis()
            )
            EscaneoUiState.Exito(punto.id)
        } else {
            EscaneoUiState.Error(R.string.error_too_far)
        }
    }
}