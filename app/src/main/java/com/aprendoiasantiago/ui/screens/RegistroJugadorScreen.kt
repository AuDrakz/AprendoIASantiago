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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
    var usuario by rememberSaveable { mutableStateOf("") }
    var contrasenaLogin by rememberSaveable { mutableStateOf("") }
    var errorLogin by rememberSaveable { mutableStateOf<Int?>(null) }
    var ingresando by remember { mutableStateOf(false) }
    var mostrarMenuRapido by remember { mutableStateOf(false) }
    var iniciado by rememberSaveable { mutableStateOf(false) }

    // Formulario de registro vacío una sola vez por visita
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
        // El círculo se adapta al ancho de la pantalla
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
                        gradiente = gradienteNeon,
                        viewModel = viewModel,
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
    gradiente: Brush,
    viewModel: SesionViewModel,
    onDatosValidos: () -> Unit,
    onVolverDatos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = diametro * 0.14f, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!eligiendoAvatar) {
            TituloCara(stringResource(R.string.acceso_titulo_registro))
            Spacer(modifier = Modifier.height(8.dp))

            CampoNeon(
                valor = registro.nombre,
                onCambio = viewModel::onNombreCambia,
                etiqueta = stringResource(R.string.registro_nombre_label),
                error = registro.errorNombre?.let { stringResource(it) }
            )
            Spacer(modifier = Modifier.height(4.dp))
            CampoNeon(
                valor = registro.email,
                onCambio = viewModel::onEmailCambia,
                etiqueta = stringResource(R.string.editar_correo_label),
                error = registro.errorEmail?.let { stringResource(it) },
                teclado = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(4.dp))
            CampoNeon(
                valor = registro.contrasena,
                onCambio = viewModel::onContrasenaCambia,
                etiqueta = stringResource(R.string.registro_contrasena_label),
                error = registro.errorContrasena?.let { stringResource(it) },
                teclado = KeyboardType.Password,
                oculto = true
            )

            Spacer(modifier = Modifier.height(8.dp))
            BotonDegradado(
                texto = stringResource(R.string.acceso_boton_registrarse),
                gradiente = gradiente,
                // Solo valida; la cuenta se crea después de elegir avatar
                onClick = { if (viewModel.validarRegistro()) onDatosValidos() }
            )
        } else {
            TituloCara(stringResource(R.string.acceso_elige_avatar))
            Spacer(modifier = Modifier.height(10.dp))

            repeat(2) { fila ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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

            Spacer(modifier = Modifier.height(8.dp))
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
                    .padding(top = 8.dp, start = 4.dp, end = 4.dp)
            )
            Text(
                text = stringResource(R.string.acceso_volver_datos),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                modifier = Modifier
                    .clickable(onClick = onVolverDatos)
                    .padding(top = 6.dp, start = 4.dp, end = 4.dp)
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
            .padding(horizontal = diametro * 0.14f, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TituloCara(stringResource(R.string.acceso_titulo_login))
        Spacer(modifier = Modifier.height(10.dp))

        CampoNeon(
            valor = usuario,
            onCambio = onUsuario,
            etiqueta = stringResource(R.string.acceso_usuario_label),
            error = null
        )
        Spacer(modifier = Modifier.height(6.dp))
        CampoNeon(
            valor = contrasena,
            onCambio = onContrasena,
            etiqueta = stringResource(R.string.registro_contrasena_label),
            error = error?.let { stringResource(it) },
            teclado = KeyboardType.Password,
            oculto = true
        )

        Spacer(modifier = Modifier.height(12.dp))
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

/** Campo con el estilo neón de la pantalla; el error sale debajo, en letra pequeña. */
@Composable
private fun CampoNeon(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    teclado: KeyboardType = KeyboardType.Text,
    oculto: Boolean = false
) {
    val acento = MaterialTheme.colorScheme.primary

    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta, color = acento, fontSize = 11.sp, maxLines = 1) },
        singleLine = true,
        isError = error != null,
        visualTransformation = if (oculto) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 15.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FondoCampo,
            unfocusedContainerColor = FondoCampo,
            focusedBorderColor = acento,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    )
    if (error != null) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            fontSize = 10.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BotonDegradado(
    texto: String,
    gradiente: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    alto: Dp = 38.dp,
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