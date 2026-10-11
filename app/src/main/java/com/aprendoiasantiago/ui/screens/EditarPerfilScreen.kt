package com.aprendoiasantiago.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aprendoiasantiago.model.PerfilUsuario
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.res.stringResource
import com.aprendoiasantiago.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    perfil: PerfilUsuario,
    onGuardar: (String, String) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by remember(perfil.id) {
        mutableStateOf(perfil.nombreUsuario)
    }

    var email by remember(perfil.id) {
        mutableStateOf(perfil.email)
    }

    var errorNombre by remember {
        mutableStateOf<String?>(null)
    }

    var errorEmail by remember {
        mutableStateOf<String?>(null)
    }

    val errNombreObligatorio = stringResource(R.string.error_nombre_obligatorio)
    val errNombreMin = stringResource(R.string.error_nombre_min)
    val errNombreMax = stringResource(R.string.error_nombre_max)
    val errCorreoObligatorio = stringResource(R.string.error_correo_obligatorio)
    val errCorreoInvalido = stringResource(R.string.error_correo_invalido)
    fun validarNombre(valor: String): String? {
        return when {
            valor.isBlank() ->
                errNombreObligatorio

            valor.trim().length < 3 ->
                errNombreMin

            valor.trim().length > 30 ->
                errNombreMax

            else -> null
        }
    }

    fun validarEmail(valor: String): String? {
        return when {
            valor.isBlank() ->
                errCorreoObligatorio

            !android.util.Patterns.EMAIL_ADDRESS
                .matcher(valor.trim())
                .matches() ->
                errCorreoInvalido

            else -> null
        }
    }

    LaunchedEffect(nombre) {
        errorNombre = validarNombre(nombre)
    }

    LaunchedEffect(email) {
        errorEmail = validarEmail(email)
    }

    val formularioValido =
        errorNombre == null &&
                errorEmail == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.editar_perfil))
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver
                    ) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.editar_nombre_label))
                },
                isError = errorNombre != null,
                supportingText = {
                    errorNombre?.let {
                        Text(it)
                    }
                },
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.editar_correo_label))
                },
                isError = errorEmail != null,
                supportingText = {
                    errorEmail?.let {
                        Text(it)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true
            )

            Button(
                onClick = {
                    onGuardar(nombre, email)
                },
                enabled = formularioValido,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    vertical = 14.dp
                )
            ) {
                Text(stringResource(R.string.editar_guardar))
            }
        }
    }
}