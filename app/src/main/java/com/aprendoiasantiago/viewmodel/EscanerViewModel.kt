package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprendoiasantiago.model.ResultadoEscaneo
import com.aprendoiasantiago.repository.PuntoRepository
import kotlinx.coroutines.launch

class EscanerViewModel (
    private val repositorio: PuntoRepository
) : ViewModel() {
    /**
     * Procesa QR leido. Corre en una corrutina porque Room no permite
     * acceder a la base de datos desde el hilo principal
     * El resultado llegar por el callback
     */
    fun escanearCodigoQR(
        codigoLeido: String,
        onResultado: (ResultadoEscaneo) -> Unit = {}
    ) {
        viewModelScope.launch {
            onResultado(repositorio.registrarVisita(codigoLeido))
        }
    }
}