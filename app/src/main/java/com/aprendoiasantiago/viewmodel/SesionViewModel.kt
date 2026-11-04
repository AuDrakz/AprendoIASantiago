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
import com.aprendoiasantiago.repository.ResultadoRegistro

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
    /** Ingreso con nombre de usuario o correo, más la contraseña. */
    fun iniciarSesionConCredencial(
        usuario: String,
        contrasena: String,
        onResultado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            onResultado(repositorio.iniciarSesionConCredencial(usuario, contrasena))
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch { repositorio.cerrarSesion() }
    }

    // --- Gestión de jugadores (solo Administrador) ---

    fun eliminarJugador(id: Int) {
        viewModelScope.launch { repositorio.eliminarJugador(id) }
    }

    /** onResultado(false) = el correo ya lo usa otro jugador. */
    fun actualizarJugador(
        id: Int,
        nombre: String,
        email: String,
        onResultado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            onResultado(repositorio.actualizarJugador(id, nombre, email))
        }
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

    /** Valida nombre, correo y contraseña. true si todo es válido (todavía no crea nada). */
    fun validarRegistro(): Boolean {
        val f = _registro.value
        val eNombre = ErroresFormulario.nombre(f.nombre)
        val eEmail = ErroresFormulario.email(f.email)
        val eContrasena = ErroresFormulario.contrasena(f.contrasena)

        _registro.update {
            it.copy(
                errorNombre = eNombre,
                errorEmail = eEmail,
                errorContrasena = eContrasena
            )
        }
        return listOf(eNombre, eEmail, eContrasena).all { it == null }
    }

    /** Crea el jugador y abre su sesión. Si el correo o el nombre ya existen, publica el error. */
    fun registrar() {
        if (_registro.value.guardando) return
        if (!validarRegistro()) return

        val f = _registro.value
        _registro.update { it.copy(guardando = true) }

        viewModelScope.launch {
            val resultado = repositorio.registrarJugador(
                nombre = f.nombre,
                email = f.email,
                avatar = f.avatar,
                contrasena = f.contrasena
            )
            when (resultado) {
                ResultadoRegistro.EXITO ->
                    _registro.value = RegistroUiState() // no deja la contraseña en memoria
                ResultadoRegistro.CORREO_DUPLICADO ->
                    _registro.update {
                        it.copy(guardando = false, errorEmail = R.string.error_correo_duplicado)
                    }
                ResultadoRegistro.NOMBRE_DUPLICADO ->
                    _registro.update {
                        it.copy(guardando = false, errorNombre = R.string.error_nombre_duplicado)
                    }
            }
        }
    }

    private fun aEstado(jugador: PerfilUsuario?): EstadoSesion =
        if (jugador == null) EstadoSesion.SinSesion
        else EstadoSesion.ConSesion(jugador.id, jugador.rol)
}