package com.aprendoiasantiago.ui.screens

import androidx.annotation.StringRes
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.viewmodel.PuntosViewModel

/**
 * Vista de gestión de contenido.
 * - Administrador (esAdmin = true): crea, edita y elimina puntos QR completos.
 * - Creador de contenido (esAdmin = false): edita solo la cápsula (nombre, tema y video).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPuntosScreen(
    esAdmin: Boolean,
    viewModel: PuntosViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var creando by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<PuntoInteres?>(null) }
    var aEliminar by remember { mutableStateOf<PuntoInteres?>(null) }

    if (creando || editando != null) {
        FormularioPuntoDialog(
            punto = editando,
            esAdmin = esAdmin,
            onCancelar = {
                creando = false
                editando = null
            },
            onGuardar = { punto, alResultado ->
                viewModel.guardarPunto(punto) { ok ->
                    if (ok) {
                        creando = false
                        editando = null
                    }
                    alResultado(ok)
                }
            }
        )
    }

    aEliminar?.let { punto ->
        AlertDialog(
            onDismissRequest = { aEliminar = null },
            title = { Text(stringResource(R.string.gestion_eliminar_punto_titulo)) },
            text = {
                Text(stringResource(R.string.gestion_eliminar_punto_texto, punto.titulo))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarPunto(punto.id)
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
                        text = stringResource(
                            if (esAdmin) R.string.gestion_puntos_titulo
                            else R.string.gestion_capsulas_titulo
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            if (esAdmin) {
                FloatingActionButton(onClick = { creando = true }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.gestion_agregar_punto)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (uiState.puntos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.lista_vacia))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = uiState.puntos, key = { it.id }) { punto ->
                    TarjetaGestion(
                        punto = punto,
                        esAdmin = esAdmin,
                        onEditar = { editando = punto },
                        onEliminar = { aEliminar = punto }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaGestion(
    punto: PuntoInteres,
    esAdmin: Boolean,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = punto.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.tarjeta_concepto_ia, punto.conceptoIA),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.gestion_qr, punto.codigoQR),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.gestion_video, punto.urlVideo),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (esAdmin) {
                    Text(
                        text = stringResource(
                            R.string.gestion_ubicacion,
                            punto.latitud,
                            punto.longitud
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.puntos_recompensa, punto.puntosRecompensa),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.gestion_editar),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (esAdmin) {
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
    }
}

@Composable
private fun FormularioPuntoDialog(
    punto: PuntoInteres?,
    esAdmin: Boolean,
    onCancelar: () -> Unit,
    onGuardar: (PuntoInteres, (Boolean) -> Unit) -> Unit
) {
    var titulo by remember { mutableStateOf(punto?.titulo ?: "") }
    var concepto by remember { mutableStateOf(punto?.conceptoIA ?: "") }
    var url by remember { mutableStateOf(punto?.urlVideo ?: "https://") }
    var latitud by remember { mutableStateOf(punto?.latitud?.toString() ?: "") }
    var longitud by remember { mutableStateOf(punto?.longitud?.toString() ?: "") }
    var codigoQr by remember { mutableStateOf(punto?.codigoQR ?: "") }
    var recompensa by remember { mutableStateOf(punto?.puntosRecompensa?.toString() ?: "100") }
    var error by remember { mutableStateOf<Int?>(null) }

    val tituloDialogo = when {
        punto == null -> R.string.gestion_nuevo_punto
        esAdmin -> R.string.gestion_editar_punto
        else -> R.string.gestion_editar_capsula
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(tituloDialogo)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoTexto(titulo, R.string.gestion_label_lugar, { titulo = it })
                CampoTexto(concepto, R.string.gestion_label_concepto, { concepto = it })
                CampoTexto(url, R.string.gestion_label_url, { url = it }, KeyboardType.Uri)

                if (esAdmin) {
                    CampoTexto(latitud, R.string.gestion_label_latitud, { latitud = it })
                    CampoTexto(longitud, R.string.gestion_label_longitud, { longitud = it })
                    CampoTexto(codigoQr, R.string.gestion_label_qr, { codigoQr = it })
                    CampoTexto(
                        recompensa,
                        R.string.gestion_label_recompensa,
                        { recompensa = it },
                        KeyboardType.Number
                    )
                }

                error?.let { mensaje ->
                    Text(
                        text = stringResource(mensaje),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lat = latitud.trim().replace(',', '.').toDoubleOrNull()
                    val lon = longitud.trim().replace(',', '.').toDoubleOrNull()
                    val pts = recompensa.trim().toIntOrNull()

                    val mensaje: Int? = when {
                        titulo.isBlank() || concepto.isBlank() -> R.string.gestion_error_campos
                        !url.trim().startsWith("http") -> R.string.gestion_error_url
                        esAdmin && (lat == null || lat !in -90.0..90.0 ||
                                lon == null || lon !in -180.0..180.0) ->
                            R.string.gestion_error_coordenadas
                        esAdmin && (pts == null || pts < 0) -> R.string.gestion_error_puntos
                        else -> null
                    }
                    error = mensaje
                    if (mensaje != null) return@TextButton

                    val base = punto ?: PuntoInteres(
                        id = 0,
                        titulo = "",
                        conceptoIA = "",
                        latitud = 0.0,
                        longitud = 0.0,
                        codigoQR = "",
                        puntosRecompensa = 0,
                        urlVideo = ""
                    )

                    var nuevo = base.copy(
                        titulo = titulo.trim(),
                        conceptoIA = concepto.trim(),
                        urlVideo = url.trim()
                    )
                    if (esAdmin) {
                        nuevo = nuevo.copy(
                            latitud = lat ?: 0.0,
                            longitud = lon ?: 0.0,
                            codigoQR = codigoQr.trim().ifBlank { generarCodigoQr(titulo) },
                            puntosRecompensa = pts ?: 0
                        )
                    }

                    onGuardar(nuevo) { ok ->
                        if (!ok) error = R.string.gestion_error_qr_duplicado
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

@Composable
private fun CampoTexto(
    valor: String,
    @StringRes etiqueta: Int,
    onCambio: (String) -> Unit,
    teclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(stringResource(etiqueta)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Genera un código QR único a partir del nombre del lugar. Ej: QR_PLAZADEARMAS_4821 */
private fun generarCodigoQr(titulo: String): String {
    val base = titulo.uppercase().filter { it.isLetterOrDigit() }.take(14)
    return "QR_${base}_${System.currentTimeMillis() % 10_000}"
}