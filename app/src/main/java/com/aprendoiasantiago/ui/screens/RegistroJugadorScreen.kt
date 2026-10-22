package com.aprendoiasantiago.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.R
import com.aprendoiasantiago.ui.components.AvatarJugador
import com.aprendoiasantiago.ui.components.IconoError
import com.aprendoiasantiago.ui.components.TOTAL_AVATARES
import com.aprendoiasantiago.viewmodel.SesionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroJugadorScreen(
    viewModel: SesionViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.registro.collectAsStateWithLifecycle()

    // Formulario vacío una sola vez por visita (no se repite al rotar la pantalla)
    var iniciado by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!iniciado) {
            viewModel.limpiarRegistro()
            iniciado = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.registro_jugador_titulo)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.registro_jugador_subtitulo),
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = estado.nombre,
                onValueChange = viewModel::onNombreCambia,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.registro_nombre_label)) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                trailingIcon = { if (estado.errorNombre != null) IconoError() },
                isError = estado.errorNombre != null,
                supportingText = { estado.errorNombre?.let { Text(stringResource(it)) } },
                singleLine = true
            )

            OutlinedTextField(
                value = estado.email,
                onValueChange = viewModel::onEmailCambia,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.editar_correo_label)) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                trailingIcon = { if (estado.errorEmail != null) IconoError() },
                isError = estado.errorEmail != null,
                supportingText = { estado.errorEmail?.let { Text(stringResource(it)) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            OutlinedTextField(
                value = estado.telefono,
                onValueChange = viewModel::onTelefonoCambia,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.registro_telefono_label)) },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                trailingIcon = { if (estado.errorTelefono != null) IconoError() },
                isError = estado.errorTelefono != null,
                supportingText = { estado.errorTelefono?.let { Text(stringResource(it)) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = viewModel::onContrasenaCambia,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.registro_contrasena_label)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = { if (estado.errorContrasena != null) IconoError() },
                isError = estado.errorContrasena != null,
                supportingText = { estado.errorContrasena?.let { Text(stringResource(it)) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            OutlinedTextField(
                value = estado.direccion,
                onValueChange = viewModel::onDireccionCambia,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.registro_direccion_label)) },
                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                singleLine = true
            )

            Text(
                text = stringResource(R.string.registro_avatar_label),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(TOTAL_AVATARES) { indice ->
                    AvatarJugador(
                        indice = indice,
                        modifier = Modifier.clickable { viewModel.onAvatarCambia(indice) },
                        seleccionado = estado.avatar == indice
                    )
                }
            }

            Button(
                onClick = viewModel::registrar,
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) { Text(stringResource(R.string.registro_crear)) }
        }
    }
}