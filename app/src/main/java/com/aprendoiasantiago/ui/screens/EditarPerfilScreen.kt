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

    fun validarNombre(valor: String): String? {
        return when {
            valor.isBlank() ->
                "El nombre es obligatorio"

            valor.trim().length < 3 ->
                "Mínimo 3 caracteres"

            valor.trim().length > 30 ->
                "Máximo 30 caracteres"

            else -> null
        }
    }

    fun validarEmail(valor: String): String? {
        return when {
            valor.isBlank() ->
                "El correo es obligatorio"

            !android.util.Patterns.EMAIL_ADDRESS
                .matcher(valor.trim())
                .matches() ->
                "Ingresa un correo válido"

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
                    Text("Editar perfil")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVolver
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
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
                    Text("Nombre de usuario")
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
                    Text("Correo electrónico")
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
                Text("Guardar cambios")
            }
        }
    }
}