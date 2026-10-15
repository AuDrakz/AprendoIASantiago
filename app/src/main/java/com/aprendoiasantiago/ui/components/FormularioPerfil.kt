package com.aprendoiasantiago.ui.components

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.Rol
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

/** Formulario reutilizable: foto, nombre, correo, rol y botón. No guarda estado propio. */
@Composable
fun FormularioPerfil(
    estado: FormularioUiState,
    @StringRes textoBoton: Int,
    onNombre: (String) -> Unit,
    onEmail: (String) -> Unit,
    onRol: (Rol) -> Unit,
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
            isError = estado.errorNombre != null,
            supportingText = { estado.errorNombre?.let { Text(stringResource(it)) } },
            singleLine = true
        )
        OutlinedTextField(
            value = estado.email,
            onValueChange = onEmail,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.editar_correo_label)) },
            isError = estado.errorEmail != null,
            supportingText = { estado.errorEmail?.let { Text(stringResource(it)) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Column(Modifier.fillMaxWidth().selectableGroup()) {
            Text(stringResource(R.string.rol_label), style = MaterialTheme.typography.labelLarge)
            Rol.entries.forEach { rol ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = estado.rol == rol,
                            onClick = { onRol(rol) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = estado.rol == rol, onClick = null)
                    Text(stringResource(rol.etiqueta), Modifier.padding(start = 12.dp))
                }
            }
        }

        Button(
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) { Text(stringResource(textoBoton)) }
    }
}