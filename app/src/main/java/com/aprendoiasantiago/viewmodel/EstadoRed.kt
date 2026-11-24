package com.aprendoiasantiago.viewmodel

import android.util.Log
import com.aprendoiasantiago.util.ErrorRed
import com.aprendoiasantiago.util.aErrorRed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Estado de carga y de error de las llamadas a los microservicios. */
class EstadoRed {

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<ErrorRed?>(null)
    val error: StateFlow<ErrorRed?> = _error.asStateFlow()

    fun limpiarError() {
        _error.value = null
    }

    /** Ejecuta la llamada. Si falla, publica el error y devuelve null. */
    suspend fun <T> ejecutar(
        etiqueta: String,
        mostrarCarga: Boolean = true,
        bloque: suspend () -> T
    ): T? {
        if (mostrarCarga) _cargando.value = true
        return try {
            bloque()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Red", "Falló: $etiqueta", e)
            _error.value = e.aErrorRed()
            null
        } finally {
            if (mostrarCarga) _cargando.value = false
        }
    }
}