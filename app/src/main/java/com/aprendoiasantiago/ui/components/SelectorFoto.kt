package com.aprendoiasantiago.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aprendoiasantiago.R
import com.aprendoiasantiago.util.ArchivosFoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Botones para tomar una foto (TakePicture) o elegirla de la galería (GetContent).
 * Entrega la ruta del archivo ya optimizado por onFotoLista.
 */
@Composable
fun SelectorFoto(
    onFotoLista: (String) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes textoCamara: Int = R.string.foto_tomar,
    permitirGaleria: Boolean = true
) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    val alFotoLista by rememberUpdatedState(onFotoLista)
    // Sobrevive a rotaciones mientras la cámara está abierta
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<Int?>(null) }

    /** Procesa la imagen en segundo plano; null = imagen inválida. */
    fun procesar(bloque: () -> String?) {
        scope.launch {
            val ruta = withContext(Dispatchers.IO) { bloque() }
            if (ruta != null) alFotoLista(ruta) else mensaje = R.string.foto_invalida
        }
    }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaPendiente
        rutaPendiente = null
        if (exito && ruta != null) {
            procesar { ArchivosFoto.optimizar(ruta) }
        } else {
            ArchivosFoto.borrar(ruta) // cancelado: borra el archivo vacío
            mensaje = R.string.foto_cancelada
        }
    }

    fun abrirCamara() {
        val archivo = ArchivosFoto.nuevoArchivo(contexto)
        rutaPendiente = archivo.absolutePath
        camara.launch(ArchivosFoto.uriDe(contexto, archivo))
    }

    val permiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara() else mensaje = R.string.foto_permiso_denegado
    }

    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) mensaje = R.string.foto_cancelada
        else procesar { ArchivosFoto.copiar(contexto, uri) }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    mensaje = null
                    val tienePermiso = ContextCompat.checkSelfPermission(
                        contexto, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (tienePermiso) abrirCamara() else permiso.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(textoCamara)) }

            if (permitirGaleria) {
                OutlinedButton(
                    onClick = {
                        mensaje = null
                        galeria.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.foto_galeria)) }
            }
        }
        mensaje?.let {
            Text(
                text = stringResource(it),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}