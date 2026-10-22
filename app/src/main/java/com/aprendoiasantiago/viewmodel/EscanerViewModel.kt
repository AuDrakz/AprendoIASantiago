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
            // Garantiza que los puntos y sus códigos QR estén sincronizados antes de buscar.
            repositorio.sembrarDatosIniciales()

            val resultado = repositorio.registrarVisita(
                codigoQR = codigoLeido,
                latitudUsuario = latitudUsuario,
                longitudUsuario = longitudUsuario,
                altitudUsuario = altitudUsuario
            )

            onResultado(resultado)
        }
    }
    fun registrarSelfie(
        codigoLeido: String,
        latitudUsuario: Double?,
        longitudUsuario: Double?,
        altitudUsuario: Double?,
        ruta: String,
        onResultado: (ResultadoEscaneo, Boolean) -> Unit
    ) {
        viewModelScope.launch {
            repositorio.sembrarDatosIniciales()
            val (resultado, bonus) = repositorio.registrarSelfie(
                codigoLeido, latitudUsuario, longitudUsuario, altitudUsuario, ruta
            )
            onResultado(resultado, bonus)
        }
    }
}