package com.aprendoiasantiago.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.R
import com.aprendoiasantiago.ui.components.FormularioPerfil
import com.aprendoiasantiago.ui.components.LiberarFormularioAlSalir
import com.aprendoiasantiago.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    viewModel: PerfilViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.formulario.collectAsStateWithLifecycle()
    val perfil = viewModel.uiState.collectAsStateWithLifecycle().value.perfil

    // Precarga cuando Room entrega el perfil (el ViewModel ignora llamadas repetidas)
    LaunchedEffect(perfil.id) { viewModel.cargarFormulario(perfil) }
    LiberarFormularioAlSalir(viewModel::cerrarFormulario)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.editar_perfil)) },
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
            textoBoton = R.string.editar_guardar,
            onNombre = viewModel::onNombreCambia,
            onEmail = viewModel::onEmailCambia,
            onRol = viewModel::onRolCambia,
            onFoto = viewModel::onFotoCambia,
            onGuardar = { viewModel.guardarFormulario(onVolver) },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )
    }
}