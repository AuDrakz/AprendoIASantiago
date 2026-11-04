package com.aprendoiasantiago.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.ui.components.FondoNeon
import com.aprendoiasantiago.ui.components.TarjetaPunto
import com.aprendoiasantiago.viewmodel.FiltroPuntos
import com.aprendoiasantiago.viewmodel.PuntosViewModel
import kotlin.math.roundToInt

/** Versión "stateful": única que conoce el ViewModel. */
@Composable
fun ListaPuntosScreen(
    viewModel: PuntosViewModel,
    onIrAEscaner: () -> Unit,
    onVerDetalle: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ListaPuntosContent(
        puntos = uiState.puntosFiltrados,
        total = uiState.puntos.size,
        descubiertos = uiState.puntos.count { it.fueVisitado },
        filtroActual = uiState.filtro,
        onFiltroChange = viewModel::cambiarFiltro,
        onEscanearClick = { onIrAEscaner() },
        onVerDetalleClick = { punto -> onVerDetalle(punto.id) }
    )
}

/** Versión "stateless": fácil de previsualizar y testear. */
@Composable
fun ListaPuntosContent(
    puntos: List<PuntoInteres>,
    filtroActual: FiltroPuntos,
    onFiltroChange: (FiltroPuntos) -> Unit,
    onEscanearClick: (PuntoInteres) -> Unit,
    onVerDetalleClick: (PuntoInteres) -> Unit,
    total: Int = puntos.size,
    descubiertos: Int = puntos.count { it.fueVisitado }
) {
    FondoNeon {
        Column(modifier = Modifier.fillMaxSize()) {
            EncabezadoLista(total = total, descubiertos = descubiertos)
            FiltrosLista(filtroActual = filtroActual, onFiltroChange = onFiltroChange)

            if (puntos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.lista_vacia),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(items = puntos, key = { it.id }) { punto ->
                        TarjetaPunto(
                            punto = punto,
                            // No descubierto: ir a escanear. Descubierto: ver la cápsula y la selfie
                            onClick = {
                                if (punto.fueVisitado) onVerDetalleClick(punto) else onEscanearClick(punto)
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoLista(total: Int, descubiertos: Int) {
    val colores = MaterialTheme.colorScheme
    val progreso by animateFloatAsState(
        targetValue = if (total == 0) 0f else descubiertos / total.toFloat(),
        animationSpec = tween(900),
        label = "progresoLista"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Place,
                contentDescription = null,
                tint = colores.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = stringResource(R.string.lista_titulo),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.lista_progreso, descubiertos, total),
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant
            )
            Text(
                text = "${(progreso * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colores.primary
            )
        }

        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            color = colores.primary,
            trackColor = colores.surfaceVariant
        )
    }
}

/** Tres filtros con el mismo ancho. */
@Composable
private fun FiltrosLista(
    filtroActual: FiltroPuntos,
    onFiltroChange: (FiltroPuntos) -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FiltroPuntos.entries.forEach { filtro ->
            FilterChip(
                selected = filtro == filtroActual,
                onClick = { onFiltroChange(filtro) },
                label = {
                    Text(
                        text = stringResource(filtro.etiqueta),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colores.primary.copy(alpha = 0.22f),
                    selectedLabelColor = colores.primary
                )
            )
        }
    }
}