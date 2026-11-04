package com.aprendoiasantiago.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.R
import com.aprendoiasantiago.ui.components.AvatarJugador
import com.aprendoiasantiago.viewmodel.RegistroUiState
import com.aprendoiasantiago.viewmodel.SesionViewModel

// Avatar de "usuario por defecto" (el ícono de persona de AvatarJugador)
private const val AVATAR_POR_DEFECTO = 4

private val FondoAcceso = Brush.verticalGradient(
    listOf(Color(0xFF00ACC1), Color(0xFF1E1E64), Color(0xFF0A0526))
)
private val FondoMoneda = Color(0xFF080723)
private val FondoCampo = Color(0xFF131038)
private val ColorError = Color(0xFFFF6B6B)

@Composable
fun RegistroJugadorScreen(
    viewModel: SesionViewModel,
    onVolver: () -> Unit
) {
    val registro by viewModel.registro.collectAsStateWithLifecycle()
    val colores = MaterialTheme.colorScheme
    // Los acentos salen del tema, así responden al modo daltonismo
    val gradienteNeon = Brush.linearGradient(listOf(colores.primary, colores.secondary))

    var esRegistro by rememberSaveable { mutableStateOf(true) }
    var eligiendoAvatar by rememberSaveable { mutableStateOf(false) }
    // Los errores del registro se muestran solo después de pulsar "Registrarse"
    var intentoRegistro by rememberSaveable { mutableStateOf(false) }
    var usuario by rememberSaveable { mutableStateOf("") }
    var contrasenaLogin by rememberSaveable { mutableStateOf("") }
    var errorLogin by rememberSaveable { mutableStateOf<Int?>(null) }
    var ingresando by remember { mutableStateOf(false) }
    var mostrarMenuRapido by remember { mutableStateOf(false) }
    var iniciado by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!iniciado) {
            viewModel.limpiarRegistro()
            iniciado = true
        }
    }

    // Si el correo o el nombre ya existen, vuelve a los datos para corregirlos
    LaunchedEffect(registro.errorEmail, registro.errorNombre) {
        if (eligiendoAvatar && (registro.errorEmail != null || registro.errorNombre != null)) {
            eligiendoAvatar = false
        }
    }

    val rotacion by animateFloatAsState(
        targetValue = if (esRegistro) 0f else 180f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "efectoMonedaGiro"
    )

    val ingresar: () -> Unit = {
        if (usuario.isBlank() || contrasenaLogin.isEmpty()) {
            errorLogin = R.string.acceso_error_campos
        } else {
            ingresando = true
            // Si es correcto, la sesión se abre y la navegación cambia sola
            viewModel.iniciarSesionConCredencial(usuario, contrasenaLogin) { ok ->
                ingresando = false
                if (!ok) errorLogin = R.string.acceso_error_credenciales
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoAcceso)
            .safeDrawingPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val diametro = maxWidth.coerceAtMost(410.dp)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 64.dp)
        ) {
            Text(
                text = if (esRegistro) "¡Bienvenido!" else "¡Hola de nuevo!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = Color.White
            )
            Text(
                text = "Aprende IA mientras exploras Santiago",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Círculo central: la moneda que gira
            Box(
                modifier = Modifier
                    .size(diametro)
                    .clip(CircleShape)
                    .background(FondoMoneda)
                    .border(width = 3.dp, brush = gradienteNeon, shape = CircleShape)
                    .graphicsLayer {
                        rotationY = rotacion
                        cameraDistance = 15f * density
                    },
                contentAlignment = Alignment.Center
            ) {
                if (rotacion <= 90f) {
                    CaraRegistro(
                        diametro = diametro,
                        registro = registro,
                        eligiendoAvatar = eligiendoAvatar,
                        mostrarErrores = intentoRegistro,
                        gradiente = gradienteNeon,
                        viewModel = viewModel,
                        onIntento = { intentoRegistro = true },
                        onDatosValidos = { eligiendoAvatar = true },
                        onVolverDatos = { eligiendoAvatar = false }
                    )
                } else {
                    // Cara trasera: se gira 180° para que no se vea en espejo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        CaraLogin(
                            diametro = diametro,
                            usuario = usuario,
                            onUsuario = { usuario = it; errorLogin = null },
                            contrasena = contrasenaLogin,
                            onContrasena = { contrasenaLogin = it; errorLogin = null },
                            error = errorLogin,
                            ingresando = ingresando,
                            gradiente = gradienteNeon,
                            onIngresar = ingresar
                        )
                    }
                }
            }
        }

        // Volver a la bienvenida
        IconButton(
            onClick = onVolver,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.volver),
                tint = Color.White
            )
        }

        // Menú rápido (inferior izquierdo): rellena el usuario de Admin o Creador
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AnimatedVisibility(
                visible = mostrarMenuRapido,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BotonDegradado(
                        texto = "Admin",
                        gradiente = gradienteNeon,
                        alto = 34.dp,
                        onClick = {
                            usuario = "Admin"
                            contrasenaLogin = "123"
                            errorLogin = null
                            esRegistro = false
                            eligiendoAvatar = false
                            mostrarMenuRapido = false
                        }
                    )
                    BotonDegradado(
                        texto = "GenCont",
                        gradiente = gradienteNeon,
                        alto = 34.dp,
                        onClick = {
                            usuario = "GenCont"
                            contrasenaLogin = "123"
                            errorLogin = null
                            esRegistro = false
                            eligiendoAvatar = false
                            mostrarMenuRapido = false
                        }
                    )
                }
            }

            IconButton(
                onClick = { mostrarMenuRapido = !mostrarMenuRapido },
                modifier = Modifier
                    .size(46.dp)
                    .background(gradienteNeon, CircleShape)
            ) {
                Icon(
                    imageVector = if (mostrarMenuRapido) Icons.Default.Person else Icons.Default.Add,
                    contentDescription = "Accesos rápidos",
                    tint = Color.White
                )
            }
        }

        // Botón inferior derecho: cambia entre registro e ingreso (gira la moneda)
        BotonDegradado(
            texto = stringResource(
                if (esRegistro) R.string.acceso_titulo_login else R.string.acceso_boton_registrarse
            ),
            gradiente = gradienteNeon,
            alto = 40.dp,
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = {
                esRegistro = !esRegistro
                eligiendoAvatar = false
                intentoRegistro = false
                errorLogin = null
            }
        )
    }
}

/** Cara 1: crear usuario (3 campos) y, después, elegir avatar. */
@Composable
private fun CaraRegistro(
    diametro: Dp,
    registro: RegistroUiState,
    eligiendoAvatar: Boolean,
    mostrarErrores: Boolean,
    gradiente: Brush,
    viewModel: SesionViewModel,
    onIntento: () -> Unit,
    onDatosValidos: () -> Unit,
    onVolverDatos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = diametro * 0.14f, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        if (!eligiendoAvatar) {
            // Solo se muestran los errores después de pulsar "Registrarse"
            val errorNombre = if (mostrarErrores) registro.errorNombre else null
            val errorEmail = if (mostrarErrores) registro.errorEmail else null
            val errorContrasena = if (mostrarErrores) registro.errorContrasena else null
            val mensaje = (errorNombre ?: errorEmail ?: errorContrasena)?.let { stringResource(it) }

            TituloCara(stringResource(R.string.acceso_titulo_registro))

            CampoNeon(
                valor = registro.nombre,
                onCambio = viewModel::onNombreCambia,
                etiqueta = stringResource(R.string.registro_nombre_label),
                icono = Icons.Filled.Person,
                error = errorNombre != null
            )
            CampoNeon(
                valor = registro.email,
                onCambio = viewModel::onEmailCambia,
                etiqueta = stringResource(R.string.editar_correo_label),
                icono = Icons.Filled.Email,
                error = errorEmail != null,
                teclado = KeyboardType.Email
            )
            CampoNeon(
                valor = registro.contrasena,
                onCambio = viewModel::onContrasenaCambia,
                etiqueta = stringResource(R.string.registro_contrasena_label),
                icono = Icons.Filled.Lock,
                error = errorContrasena != null,
                teclado = KeyboardType.Password,
                oculto = true,
                accion = ImeAction.Done
            )

            // Altura fija: el mensaje no mueve el resto de la pantalla
            LineaMensaje(mensaje)

            BotonDegradado(
                texto = stringResource(R.string.acceso_boton_registrarse),
                gradiente = gradiente,
                // Solo valida; la cuenta se crea después de elegir avatar
                onClick = {
                    onIntento()
                    if (viewModel.validarRegistro()) onDatosValidos()
                }
            )
        } else {
            TituloCara(stringResource(R.string.acceso_elige_avatar))

            repeat(2) { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) { columna ->
                        val indice = fila * 3 + columna
                        AvatarJugador(
                            indice = indice,
                            modifier = Modifier.clickable { viewModel.onAvatarCambia(indice) },
                            tamano = 52.dp,
                            seleccionado = registro.avatar == indice
                        )
                    }
                }
            }

            BotonDegradado(
                texto = stringResource(R.string.acceso_crear_cuenta),
                gradiente = gradiente,
                habilitado = !registro.guardando,
                onClick = viewModel::registrar
            )
            Text(
                text = stringResource(R.string.acceso_omitir_avatar),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                modifier = Modifier
                    .clickable(enabled = !registro.guardando) {
                        viewModel.onAvatarCambia(AVATAR_POR_DEFECTO)
                        viewModel.registrar()
                    }
                    .padding(horizontal = 4.dp)
            )
            Text(
                text = stringResource(R.string.acceso_volver_datos),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                modifier = Modifier
                    .clickable(onClick = onVolverDatos)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

/** Cara 2: ingresar con nombre o correo, más la contraseña. */
@Composable
private fun CaraLogin(
    diametro: Dp,
    usuario: String,
    onUsuario: (String) -> Unit,
    contrasena: String,
    onContrasena: (String) -> Unit,
    error: Int?,
    ingresando: Boolean,
    gradiente: Brush,
    onIngresar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = diametro * 0.14f, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        TituloCara(stringResource(R.string.acceso_titulo_login))

        CampoNeon(
            valor = usuario,
            onCambio = onUsuario,
            etiqueta = stringResource(R.string.acceso_usuario_label),
            icono = Icons.Filled.Person,
            error = error != null
        )
        CampoNeon(
            valor = contrasena,
            onCambio = onContrasena,
            etiqueta = stringResource(R.string.registro_contrasena_label),
            icono = Icons.Filled.Lock,
            error = error != null,
            teclado = KeyboardType.Password,
            oculto = true,
            accion = ImeAction.Done,
            onListo = onIngresar
        )

        LineaMensaje(error?.let { stringResource(it) })

        BotonDegradado(
            texto = stringResource(R.string.acceso_boton_ingresar),
            gradiente = gradiente,
            habilitado = !ingresando,
            onClick = onIngresar
        )
    }
}

@Composable
private fun TituloCara(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        color = Color.White
    )
}

/** Mensaje de error de una sola línea y altura fija: aparezca o no, no mueve nada. */
@Composable
private fun LineaMensaje(texto: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp),
        contentAlignment = Alignment.Center
    ) {
        if (texto != null) {
            Text(
                text = texto,
                color = ColorError,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Campo de 48 dp con ícono y sugerencia dentro (sin etiqueta flotante).
 * Con error: borde rojo e ícono de advertencia (no depende solo del color).
 */
@Composable
private fun CampoNeon(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: Boolean,
    teclado: KeyboardType = KeyboardType.Text,
    oculto: Boolean = false,
    accion: ImeAction = ImeAction.Next,
    onListo: (() -> Unit)? = null
) {
    val acento = MaterialTheme.colorScheme.primary
    val foco = LocalFocusManager.current
    var enFoco by remember { mutableStateOf(false) }
    val forma = RoundedCornerShape(24.dp)

    val colorBorde = when {
        error -> ColorError
        enFoco -> acento
        else -> Color.White.copy(alpha = 0.18f)
    }

    BasicTextField(
        value = valor,
        onValueChange = onCambio,
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
        cursorBrush = SolidColor(acento),
        visualTransformation = if (oculto) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = teclado, imeAction = accion),
        keyboardActions = KeyboardActions(
            onNext = { foco.moveFocus(FocusDirection.Down) },
            onDone = {
                foco.clearFocus()
                onListo?.invoke()
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .onFocusChanged { enFoco = it.isFocused },
        decorationBox = { campoInterno ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FondoCampo, forma)
                    .border(if (error || enFoco) 1.5.dp else 1.dp, colorBorde, forma)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = if (error) ColorError else acento,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (valor.isEmpty()) {
                        Text(
                            text = etiqueta,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                    campoInterno()
                }
                if (error) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = ColorError,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    )
}

@Composable
private fun BotonDegradado(
    texto: String,
    gradiente: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    alto: Dp = 40.dp,
    habilitado: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        modifier = modifier
            .height(alto)
            .background(gradiente, RoundedCornerShape(alto / 2))
    ) {
        Text(
            text = texto,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}