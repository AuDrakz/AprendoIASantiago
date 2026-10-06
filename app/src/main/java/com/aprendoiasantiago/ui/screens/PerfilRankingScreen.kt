package com.aprendoiasantiago.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.viewmodel.AprendoIaViewModel

@Composable
fun PerfilRankingScreen(
    viewModel: AprendoIaViewModel,
    onEditarPerfil: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PerfilRankingContent(
        perfil = uiState.perfil,
        ranking = uiState.ranking,
        onEditarPerfil = onEditarPerfil
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilRankingContent(
    perfil: PerfilUsuario,
    ranking: List<EntradaRanking>,
    onEditarPerfil: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text("Perfil y ranking")
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                TarjetaResumen(
                    perfil = perfil,
                    onEditarPerfil = onEditarPerfil
                )
            }

            item {
                Text(
                    text = "Ranking",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            itemsIndexed(
                ranking,
                key = { _, entrada ->
                    entrada.nombre
                }
            ) { indice, entrada ->

                FilaRanking(
                    posicion = indice + 1,
                    entrada = entrada
                )
            }
        }
    }
}

@Composable
private fun TarjetaResumen(
    perfil: PerfilUsuario,
    onEditarPerfil: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                perfil.nombreUsuario,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                perfil.email,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Metrica(
                    valor = perfil.puntosTotales,
                    etiqueta = "Puntos"
                )

                Metrica(
                    valor = perfil.lugaresDescubiertos,
                    etiqueta = "Lugares"
                )
            }

            Spacer(
                Modifier.height(16.dp)
            )

            OutlinedButton(
                onClick = onEditarPerfil,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar perfil")
            }
        }
    }
}

@Composable
private fun Metrica(
    valor: Int,
    etiqueta: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "$valor",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            etiqueta,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun FilaRanking(
    posicion: Int,
    entrada: EntradaRanking
) {
    ListItem(
        modifier = Modifier.clip(
            RoundedCornerShape(12.dp)
        ),
        leadingContent = {
            Text(
                "$posicion",
                style = MaterialTheme.typography.titleLarge
            )
        },
        headlineContent = {
            Text(
                text = entrada.nombre,
                fontWeight =
                    if (entrada.esUsuarioActual)
                        FontWeight.Bold
                    else
                        FontWeight.Normal
            )
        },
        trailingContent = {
            Text("${entrada.puntos} pts")
        },
        colors = ListItemDefaults.colors(
            containerColor =
                if (entrada.esUsuarioActual)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
        )
    )
}