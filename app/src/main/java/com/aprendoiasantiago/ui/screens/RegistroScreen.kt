package com.aprendoiasantiago.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.R
import com.aprendoiasantiago.ui.components.FormularioPerfil
import com.aprendoiasantiago.ui.components.LiberarFormularioAlSalir
import com.aprendoiasantiago.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.formulario.collectAsStateWithLifecycle()
    var confirmando by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.limpiarFormulario() }
    LiberarFormularioAlSalir(viewModel::cerrarFormulario)

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text(stringResource(R.string.registro_confirmar_titulo)) },
            text = { Text(stringResource(R.string.registro_confirmar_texto)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmando = false
                    viewModel.guardarFormulario(onVolver)
                }) { Text(stringResource(R.string.registro_confirmar_si)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) {
                    Text(stringResource(R.string.registro_confirmar_no))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.registro_titulo)) },
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
        FormularioPerfil(
            estado = estado,
            textoBoton = R.string.registro_boton,
            onNombre = viewModel::onNombreCambia,
            onEmail = viewModel::onEmailCambia,
            onRol = viewModel::onRolCambia,
            onFoto = viewModel::onFotoCambia,
            // Valida primero; solo si es válido pide confirmación
            onGuardar = { if (viewModel.validarFormulario()) confirmando = true },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )
    }
}