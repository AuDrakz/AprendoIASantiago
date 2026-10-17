package com.aprendoiasantiago.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.R
import com.aprendoiasantiago.data.local.UserPreferencesRepository
import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.ui.components.ImagenInteligente
import com.aprendoiasantiago.viewmodel.PerfilViewModel
import kotlinx.coroutines.launch

@Composable
fun PerfilRankingScreen(
    viewModel: PerfilViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    onEditarPerfil: () -> Unit,
    onRegistro: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val altoContrasteActivo by userPreferencesRepository.modoAltoContraste.collectAsState(initial = false)
    val alcance = rememberCoroutineScope()

    PerfilRankingContent(
        perfil = uiState.perfil,
        ranking = uiState.ranking,
        altoContrasteActivo = altoContrasteActivo,
        onAltoContrasteCambiado = { nuevoEstado ->
            alcance.launch {
                userPreferencesRepository.guardarModoAltoContraste(nuevoEstado)
            }
        },
        onEditarPerfil = onEditarPerfil,
        onRegistro = onRegistro
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilRankingContent(
    perfil: PerfilUsuario,
    ranking: List<EntradaRanking>,
    altoContrasteActivo: Boolean,
    onAltoContrasteCambiado: (Boolean) -> Unit,
    onEditarPerfil: () -> Unit,
    onRegistro: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.perfil_titulo)) }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                TarjetaResumen(
                    perfil = perfil,
                    altoContrasteActivo = altoContrasteActivo,
                    onAltoContrasteCambiado = onAltoContrasteCambiado,
                    onEditarPerfil = onEditarPerfil,
                    onRegistro = onRegistro
                )
            }
            item {
                Text(
                    stringResource(R.string.perfil_ranking),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            itemsIndexed(
                ranking,
                key = { _, entrada ->
                    if (entrada.esUsuarioActual) "usuario_actual" else "base_${entrada.nombre}"
                }
            ) { indice, entrada ->
                FilaRanking(posicion = indice + 1, entrada = entrada)
            }
        }
    }
}

@Composable
private fun TarjetaResumen(
    perfil: PerfilUsuario,
    altoContrasteActivo: Boolean,
    onAltoContrasteCambiado: (Boolean) -> Unit,
    onEditarPerfil: () -> Unit,
    onRegistro: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImagenInteligente(
                    ruta = perfil.fotoRuta,
                    tamano = 72.dp,
                    descripcion = stringResource(R.string.foto_perfil)
                )
                Column {
                    Text(perfil.nombreUsuario, style = MaterialTheme.typography.headlineSmall)
                    Text(perfil.email, style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(perfil.rol.etiqueta), style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Metrica(perfil.puntosTotales, stringResource(R.string.perfil_metrica_puntos))
                Metrica(perfil.lugaresDescubiertos, stringResource(R.string.perfil_metrica_lugares))
            }

            Spacer(Modifier.height(16.dp))

            // Interruptor de Alto Contraste (DataStore)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Modo Alto Contraste (Luz Solar)",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = altoContrasteActivo,
                    onCheckedChange = onAltoContrasteCambiado
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(onClick = onEditarPerfil, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.editar_perfil))
            }
            TextButton(onClick = onRegistro, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.registro_titulo))
            }
        }
    }
}

@Composable
private fun Metrica(valor: Int, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$valor", style = MaterialTheme.typography.headlineMedium)
        Text(etiqueta, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun FilaRanking(posicion: Int, entrada: EntradaRanking) {
    ListItem(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        leadingContent = { Text("$posicion", style = MaterialTheme.typography.titleLarge) },
        headlineContent = {
            Text(
                text = entrada.nombre,
                fontWeight = if (entrada.esUsuarioActual) FontWeight.Bold else FontWeight.Normal
            )
        },
        trailingContent = { Text(stringResource(R.string.ranking_pts, entrada.puntos)) },
        colors = ListItemDefaults.colors(
            containerColor = if (entrada.esUsuarioActual)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    )
}