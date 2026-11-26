@file:OptIn(ExperimentalCoroutinesApi::class)

package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import com.aprendoiasantiago.MainDispatcherRule
import com.aprendoiasantiago.repositorioFalso
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AprendoIaViewModelFactoryTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private class ViewModelDesconocido : ViewModel()

    private val factory = AprendoIaViewModelFactory(repositorioFalso())

    @Test
    fun create_construyeCadaViewModelConocido() {
        assertTrue(factory.create(PuntosViewModel::class.java) is PuntosViewModel)
        assertTrue(factory.create(PerfilViewModel::class.java) is PerfilViewModel)
        assertTrue(factory.create(EscanerViewModel::class.java) is EscanerViewModel)
        assertTrue(factory.create(SesionViewModel::class.java) is SesionViewModel)
    }

    @Test
    fun create_conClaseDesconocida_lanzaIllegalArgumentException() {
        val error = runCatching { factory.create(ViewModelDesconocido::class.java) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }
}
