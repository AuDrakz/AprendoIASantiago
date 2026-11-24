package com.aprendoiasantiago.ui.components

import android.widget.Toast
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.viewmodel.EstadoRed

/** Muestra el círculo de carga y el mensaje de error de red. */
@Composable
fun AvisoRed(red: EstadoRed) {
    val cargando by red.cargando.collectAsStateWithLifecycle()
    val error by red.error.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(contexto, contexto.getString(it.mensaje), Toast.LENGTH_LONG).show()
            red.limpiarError()
        }
    }

    if (cargando) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            CircularProgressIndicator()
        }
    }
}