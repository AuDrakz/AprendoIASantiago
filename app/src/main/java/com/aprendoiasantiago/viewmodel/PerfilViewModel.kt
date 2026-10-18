package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.Rol
import com.aprendoiasantiago.repository.PuntoRepository
import com.aprendoiasantiago.util.ArchivosFoto
import com.aprendoiasantiago.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repositorio: PuntoRepository
) : ViewModel() {

    val uiState: StateFlow<PerfilUiState> = combine(
        repositorio.perfil,
        repositorio.rankingBase
    ) { perfil, rankingBase ->
        PerfilUiState(perfil = perfil, rankingBase = rankingBase)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PerfilUiState()
    )

    private val _formulario = MutableStateFlow(FormularioUiState())
    val formulario: StateFlow<FormularioUiState> = _formulario.asStateFlow()

    // Vive en memoria del ViewModel: sobrevive a rotaciones y se reinicia si Android cierra el proceso
    private var formularioCargado = false

    /** Para editar: precarga el formulario una sola vez por visita. */
    fun cargarFormulario(perfil: PerfilUsuario) {
        if (formularioCargado || perfil.id == 0) return // id 0 = Room aún no entrega el perfil
        formularioCargado = true
        _formulario.value = FormularioUiState(
            nombre = perfil.nombreUsuario,
            email = perfil.email,
            rol = perfil.rol,
            fotoRuta = perfil.fotoRuta
        )
    }

    /** Para registrar: formulario vacío, una sola vez por visita. */
    fun limpiarFormulario() {
        if (formularioCargado) return
        formularioCargado = true
        _formulario.value = FormularioUiState()
    }

    /** Al salir de la pantalla (no al rotar) libera el formulario para la próxima visita. */
    fun cerrarFormulario() {
        formularioCargado = false
    }

    fun onNombreCambia(valor: String) =
        _formulario.update { it.copy(nombre = valor, errorNombre = errorNombre(valor)) }

    fun onEmailCambia(valor: String) =
        _formulario.update { it.copy(email = valor, errorEmail = errorEmail(valor)) }

    fun onRolCambia(valor: Rol) = _formulario.update { it.copy(rol = valor) }

    fun onFotoCambia(ruta: String?) {
        val anterior = _formulario.value.fotoRuta
        // Borra la foto anterior solo si era un borrador (no la que ya está guardada en Room)
        if (anterior != null && anterior != uiState.value.perfil.fotoRuta) {
            ArchivosFoto.borrar(anterior)
        }
        _formulario.update { it.copy(fotoRuta = ruta) }
    }

    /** Valida todos los campos y publica los errores. true si todo es válido. */
    fun validarFormulario(): Boolean {
        val f = _formulario.value
        val eNombre = errorNombre(f.nombre)
        val eEmail = errorEmail(f.email)
        _formulario.update { it.copy(errorNombre = eNombre, errorEmail = eEmail) }
        return eNombre == null && eEmail == null
    }

    /** Valida y guarda en Room. Llama onGuardado solo si el formulario es válido. */
    fun guardarFormulario(onGuardado: () -> Unit) {
        if (!validarFormulario()) return
        val f = _formulario.value
        viewModelScope.launch {
            repositorio.actualizarPerfil(f.nombre, f.email, f.rol, f.fotoRuta)
            onGuardado()
        }
    }

    private fun errorNombre(valor: String): Int? = when {
        valor.isBlank() -> R.string.error_nombre_obligatorio
        valor.trim().length < 3 -> R.string.error_nombre_min
        valor.trim().length > 30 -> R.string.error_nombre_max
        else -> null
    }

    private fun errorEmail(valor: String): Int? = when {
        valor.isBlank() -> R.string.error_correo_obligatorio
        !Validaciones.emailValido(valor) -> R.string.error_correo_invalido
        else -> null
    }
}