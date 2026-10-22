package com.aprendoiasantiago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.repository.PuntoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Sesión, selección de jugador y registro de nuevos Clientes. */
class SesionViewModel(
    private val repositorio: PuntoRepository
) : ViewModel() {

    val estado: StateFlow<EstadoSesion> = repositorio.jugadorActivo
        .map { jugador -> aEstado(jugador) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EstadoSesion.Cargando
        )

    val jugadores: StateFlow<List<PerfilUsuario>> = repositorio.jugadores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _registro = MutableStateFlow(RegistroUiState())
    val registro: StateFlow<RegistroUiState> = _registro.asStateFlow()

    /** Si la contraseña es correcta abre la sesión; la navegación reacciona sola. */
    fun iniciarSesion(jugadorId: Int, contrasena: String, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResultado(repositorio.iniciarSesion(jugadorId, contrasena))
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch { repositorio.cerrarSesion() }
    }

    // --- Registro de Cliente ---

    fun limpiarRegistro() {
        _registro.value = RegistroUiState()
    }

    fun onNombreCambia(valor: String) =
        _registro.update { it.copy(nombre = valor, errorNombre = ErroresFormulario.nombre(valor)) }

    fun onEmailCambia(valor: String) =
        _registro.update { it.copy(email = valor, errorEmail = ErroresFormulario.email(valor)) }

    fun onTelefonoCambia(valor: String) =
        _registro.update { it.copy(telefono = valor, errorTelefono = ErroresFormulario.telefono(valor)) }

    fun onContrasenaCambia(valor: String) =
        _registro.update { it.copy(contrasena = valor, errorContrasena = ErroresFormulario.contrasena(valor)) }

    fun onDireccionCambia(valor: String) =
        _registro.update { it.copy(direccion = valor) }

    fun onAvatarCambia(valor: Int) =
        _registro.update { it.copy(avatar = valor) }

    /** Valida todos los campos; si son válidos crea el jugador y abre su sesión. */
    fun registrar() {
        val f = _registro.value
        if (f.guardando) return

        val eNombre = ErroresFormulario.nombre(f.nombre)
        val eEmail = ErroresFormulario.email(f.email)
        val eTelefono = ErroresFormulario.telefono(f.telefono)
        val eContrasena = ErroresFormulario.contrasena(f.contrasena)

        _registro.update {
            it.copy(
                errorNombre = eNombre,
                errorEmail = eEmail,
                errorTelefono = eTelefono,
                errorContrasena = eContrasena
            )
        }
        if (listOf(eNombre, eEmail, eTelefono, eContrasena).any { it != null }) return

        _registro.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val creado = repositorio.registrarJugador(
                nombre = f.nombre,
                email = f.email,
                telefono = f.telefono,
                direccion = f.direccion,
                avatar = f.avatar,
                contrasena = f.contrasena
            )
            if (creado) {
                _registro.value = RegistroUiState() // no deja la contraseña en memoria
            } else {
                _registro.update {
                    it.copy(guardando = false, errorEmail = R.string.error_correo_duplicado)
                }
            }
        }
    }

    private fun aEstado(jugador: PerfilUsuario?): EstadoSesion =
        if (jugador == null) EstadoSesion.SinSesion
        else EstadoSesion.ConSesion(jugador.id, jugador.rol)
}