
package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprendoiasantiago.model.ResultadoEscaneo
import com.aprendoiasantiago.repository.PuntoRepository
import kotlinx.coroutines.launch

class EscanerViewModel(
    private val repositorio: PuntoRepository
) : ViewModel() {

    fun escanearCodigoQR(
        codigoLeido: String,
        latitudUsuario: Double?,
        longitudUsuario: Double?,
        onResultado: (ResultadoEscaneo) -> Unit = {},
        altitudUsuario: Double? = null
    ) {
        viewModelScope.launch {
            val resultado = repositorio.registrarVisita(
                codigoQR = codigoLeido,
                latitudUsuario = latitudUsuario,
                longitudUsuario = longitudUsuario,
                altitudUsuario = altitudUsuario
            )

            onResultado(resultado)
        }
    }
}
