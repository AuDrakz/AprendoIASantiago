package com.aprendoiasantiago.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.ui.components.ImagenInteligente
import com.aprendoiasantiago.ui.components.SelectorFoto

/**
 * Detalle de un punto: concepto de IA, cápsula de video y selfie de validación.
 * Recibe el punto ya buscado (puede ser null si el id no existe).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleVideoScreen(
    punto: PuntoInteres?,
    onVolver: () -> Unit,
    onSelfie: (String) -> Unit
) {
    val contexto = LocalContext.current

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detalle_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.volver))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (punto == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.detalle_no_encontrado))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(punto.titulo, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = punto.conceptoIA,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.puntos_recompensa, punto.puntosRecompensa),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        // Abre el video en la app que corresponda (navegador, YouTube...)
                        runCatching {
                            contexto.startActivity(
                                Intent(Intent.ACTION_VIEW, punto.urlVideo.toUri())
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.detalle_ver_video))
                }

                // Selfie de validación: solo si el punto ya fue visitado
                if (punto.fueVisitado) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                stringResource(R.string.selfie_titulo),
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (punto.selfieRuta == null) {
                                Text(
                                    stringResource(R.string.selfie_descripcion),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            ImagenInteligente(
                                ruta = punto.selfieRuta,
                                tamano = 120.dp,
                                descripcion = stringResource(R.string.selfie_titulo)
                            )
                            SelectorFoto(
                                onFotoLista = onSelfie,
                                textoCamara = R.string.selfie_tomar,
                                permitirGaleria = false
                            )
                        }
                    }
                }
            }
        }
    }
}