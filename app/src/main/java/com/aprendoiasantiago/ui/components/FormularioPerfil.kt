package com.aprendoiasantiago.ui.components

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aprendoiasantiago.R
import com.aprendoiasantiago.viewmodel.FormularioUiState

/** Llama onSalir al abandonar la pantalla, pero no cuando solo rota. */
@Composable
fun LiberarFormularioAlSalir(onSalir: () -> Unit) {
    val actividad = LocalContext.current as? Activity
    val salir by rememberUpdatedState(onSalir)
    DisposableEffect(Unit) {
        onDispose { if (actividad?.isChangingConfigurations != true) salir() }
    }
}

/** Formulario de edición: foto, nombre, correo y botón. No guarda estado propio. */
@Composable
fun FormularioPerfil(
    estado: FormularioUiState,
    @StringRes textoBoton: Int,
    onNombre: (String) -> Unit,
    onEmail: (String) -> Unit,
    onFoto: (String) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ImagenInteligente(
            ruta = estado.fotoRuta,
            tamano = 120.dp,
            descripcion = stringResource(R.string.foto_perfil)
        )
        SelectorFoto(onFotoLista = onFoto)

        OutlinedTextField(
            value = estado.nombre,
            onValueChange = onNombre,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.editar_nombre_label)) },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            trailingIcon = { if (estado.errorNombre != null) IconoError() },
            isError = estado.errorNombre != null,
            supportingText = { estado.errorNombre?.let { Text(stringResource(it)) } },
            singleLine = true
        )
        OutlinedTextField(
            value = estado.email,
            onValueChange = onEmail,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.editar_correo_label)) },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            trailingIcon = { if (estado.errorEmail != null) IconoError() },
            isError = estado.errorEmail != null,
            supportingText = { estado.errorEmail?.let { Text(stringResource(it)) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Button(
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) { Text(stringResource(textoBoton)) }
    }
}

@Composable
fun IconoError() {
    Icon(
        Icons.Filled.Warning,
        contentDescription = null, // el mensaje de error debajo del campo ya lo describe
        tint = MaterialTheme.colorScheme.error
    )
}