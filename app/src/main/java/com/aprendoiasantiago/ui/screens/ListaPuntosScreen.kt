package com.aprendoiasantiago.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.ui.components.TarjetaPunto
import com.aprendoiasantiago.viewmodel.AprendoIaViewModel
import com.aprendoiasantiago.viewmodel.FiltroPuntos

/** Versión "stateful": única que conoce el ViewModel. */
@Composable
fun ListaPuntosScreen(
    viewModel: AprendoIaViewModel,
    onIrAEscaner: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ListaPuntosContent(
        puntos = uiState.puntosFiltrados,
        filtroActual = uiState.filtro,
        onFiltroChange = viewModel::cambiarFiltro,
        // Ya no simula: lleva al escáner con la cámara real
        onEscanearClick = { onIrAEscaner() }
    )
}

/** Versión "stateless": fácil de previsualizar y testear. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaPuntosContent(
    puntos: List<PuntoInteres>,
    filtroActual: FiltroPuntos,
    onFiltroChange: (FiltroPuntos) -> Unit,
    onEscanearClick: (PuntoInteres) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Puntos de interés") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            // Fila de filtros
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroPuntos.entries.forEach { filtro ->
                    FilterChip(
                        selected = filtro == filtroActual,
                        onClick = { onFiltroChange(filtro) },
                        label = { Text(filtro.etiqueta) }
                    )
                }
            }

            if (puntos.isEmpty()) {
                // Estado vacío (ej. filtro "Visitados" sin ninguno aún)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay puntos en esta categoría")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // key = id: Compose identifica cada fila aunque cambie el orden/filtro
                    items(items = puntos, key = { it.id }) { punto ->
                        TarjetaPunto(
                            punto = punto,
                            onEscanearClick = { onEscanearClick(punto) },
                            // Anima entradas, salidas y reordenamientos
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}