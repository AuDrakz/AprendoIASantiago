package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aprendoiasantiago.repository.PuntoRepository

class AprendoIaViewModelFactory(
    private val repositorio: PuntoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(PuntosViewModel::class.java) -> PuntosViewModel(repositorio)
        modelClass.isAssignableFrom(PerfilViewModel::class.java) -> PerfilViewModel(repositorio)
        modelClass.isAssignableFrom(EscanerViewModel::class.java) -> EscanerViewModel(repositorio)
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    } as T
}