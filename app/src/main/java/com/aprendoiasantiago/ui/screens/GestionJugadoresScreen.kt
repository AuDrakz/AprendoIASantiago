package com.aprendoiasantiago.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.Rol
import com.aprendoiasantiago.ui.components.AvatarJugador
import com.aprendoiasantiago.viewmodel.ErroresFormulario
import com.aprendoiasantiago.viewmodel.SesionViewModel

/** Vista exclusiva del Administrador: editar y eliminar jugadores (Clientes). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionJugadoresScreen(viewModel: SesionViewModel) {
    val jugadores by viewModel.jugadores.collectAsStateWithLifecycle()
    val clientes = jugadores.filter { it.rol == Rol.CLIENTE }

    var editando by remember { mutableStateOf<PerfilUsuario?>(null) }
    var aEliminar by remember { mutableStateOf<PerfilUsuario?>(null) }

    editando?.let { jugador ->
        DialogoEditarJugador(
            jugador = jugador,
            onCancelar = { editando = null },
            onGuardar = { nombre, email, alResultado ->
                viewModel.actualizarJugador(jugador.id, nombre, email) { ok ->
                    if (ok) editando = null
                    alResultado(ok)
                }
            }
        )
    }

    aEliminar?.let { jugador ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            title = { Text(stringResource(R.string.gestion_eliminar_jugador_titulo)) },
            text = {
                Text(stringResource(R.string.gestion_eliminar_jugador_texto, jugador.nombreUsuario))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarJugador(jugador.id)
                        aEliminar = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.gestion_eliminar),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { aEliminar = null }) {
                    Text(stringResource(R.string.gestion_cancelar))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.gestion_jugadores_titulo),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        if (clientes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.gestion_sin_jugadores))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = clientes, key = { it.id }) { jugador ->
                    TarjetaJugadorAdmin(
                        jugador = jugador,
                        onEditar = { editando = jugador },
                        onEliminar = { aEliminar = jugador }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaJugadorAdmin(
    jugador: PerfilUsuario,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarJugador(
                indice = jugador.avatar,
                fotoRuta = jugador.fotoRuta,
                tamano = 52.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jugador.nombreUsuario,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = jugador.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEditar) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.gestion_editar),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.gestion_eliminar),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DialogoEditarJugador(
    jugador: PerfilUsuario,
    onCancelar: () -> Unit,
    onGuardar: (String, String, (Boolean) -> Unit) -> Unit
) {
    var nombre by remember { mutableStateOf(jugador.nombreUsuario) }
    var email by remember { mutableStateOf(jugador.email) }
    var errorNombre by remember { mutableStateOf<Int?>(null) }
    var errorEmail by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.gestion_editar_jugador)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        errorNombre = ErroresFormulario.nombre(it)
                    },
                    label = { Text(stringResource(R.string.editar_nombre_label)) },
                    isError = errorNombre != null,
                    supportingText = {
                        errorNombre?.let { Text(stringResource(it)) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorEmail = ErroresFormulario.email(it)
                    },
                    label = { Text(stringResource(R.string.editar_correo_label)) },
                    isError = errorEmail != null,
                    supportingText = {
                        errorEmail?.let { Text(stringResource(it)) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val eNombre = ErroresFormulario.nombre(nombre)
                    val eEmail = ErroresFormulario.email(email)
                    errorNombre = eNombre
                    errorEmail = eEmail
                    if (eNombre == null && eEmail == null) {
                        onGuardar(nombre, email) { ok ->
                            if (!ok) errorEmail = R.string.error_correo_duplicado
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.gestion_guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(stringResource(R.string.gestion_cancelar))
            }
        }
    )
}