package com.aprendoiasantiago.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.ui.components.AvatarJugador
import com.aprendoiasantiago.ui.components.IconoError
import com.aprendoiasantiago.viewmodel.SesionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionJugadorScreen(
    viewModel: SesionViewModel,
    onCrearJugador: () -> Unit,
    onVolver: () -> Unit
) {
    val jugadores by viewModel.jugadores.collectAsStateWithLifecycle()
    var elegidoId by rememberSaveable { mutableStateOf<Int?>(null) }
    val elegido = jugadores.firstOrNull { it.id == elegidoId }

    elegido?.let { jugador ->
        DialogoContrasena(
            jugador = jugador,
            onCancelar = { elegidoId = null },
            onEntrar = { contrasena, alResultado ->
                viewModel.iniciarSesion(jugador.id, contrasena, alResultado)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.seleccion_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = jugadores, key = { it.id }) { jugador ->
                TarjetaJugador(jugador = jugador, onClick = { elegidoId = jugador.id })
            }
            item {
                TarjetaNuevoJugador(onClick = onCrearJugador)
            }
        }
    }
}

@Composable
private fun TarjetaJugador(jugador: PerfilUsuario, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarJugador(indice = jugador.avatar, fotoRuta = jugador.fotoRuta)
            Column {
                Text(jugador.nombreUsuario, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(jugador.rol.etiqueta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                jugador.alias?.let { alias ->
                    Text(
                        text = stringResource(R.string.seleccion_alias, alias),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaNuevoJugador(onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
            Text(
                text = stringResource(R.string.seleccion_nuevo),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/** Pide la contraseña del jugador elegido. Si es incorrecta muestra el error. */
@Composable
private fun DialogoContrasena(
    jugador: PerfilUsuario,
    onCancelar: () -> Unit,
    onEntrar: (String, (Boolean) -> Unit) -> Unit
) {
    var contrasena by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.login_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.login_para, jugador.nombreUsuario))
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = {
                        contrasena = it
                        error = false
                    },
                    label = { Text(stringResource(R.string.login_contrasena_label)) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = { if (error) IconoError() },
                    isError = error,
                    supportingText = { if (error) Text(stringResource(R.string.login_error)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onEntrar(contrasena) { ok -> error = !ok } }) {
                Text(stringResource(R.string.login_entrar))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(stringResource(R.string.login_cancelar))
            }
        }
    )
}