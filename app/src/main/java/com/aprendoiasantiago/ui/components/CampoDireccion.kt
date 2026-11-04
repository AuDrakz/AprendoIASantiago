package com.aprendoiasantiago.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aprendoiasantiago.R
import com.aprendoiasantiago.data.remote.BuscadorDirecciones
import com.aprendoiasantiago.data.remote.SugerenciaDireccion
import kotlinx.coroutines.delay

/** Campo que sugiere lugares y calles mientras escribes (como Google Maps o Waze). */
@Composable
fun CampoDireccion(
    onSeleccion: (SugerenciaDireccion) -> Unit,
    modifier: Modifier = Modifier
) {
    var texto by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf<List<SugerenciaDireccion>>(emptyList()) }
    var buscando by remember { mutableStateOf(false) }
    var terminoBusqueda by remember { mutableStateOf(false) }
    var elegido by remember { mutableStateOf(false) }

    // Espera 400 ms sin escribir antes de consultar
    LaunchedEffect(texto) {
        terminoBusqueda = false
        if (elegido || texto.trim().length < 3) {
            sugerencias = emptyList()
            buscando = false
            return@LaunchedEffect
        }
        buscando = true
        delay(400)
        sugerencias = BuscadorDirecciones.buscar(texto)
        buscando = false
        terminoBusqueda = true
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = texto,
            onValueChange = {
                elegido = false
                texto = it
            },
            label = { Text(stringResource(R.string.gestion_label_direccion)) },
            supportingText = { Text(stringResource(R.string.gestion_direccion_ayuda)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (buscando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        sugerencias.forEach { sugerencia ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        elegido = true
                        texto = listOf(sugerencia.nombre, sugerencia.direccion)
                            .filter { it.isNotBlank() }
                            .joinToString(", ")
                        sugerencias = emptyList()
                        onSeleccion(sugerencia)
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(
                        text = sugerencia.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (sugerencia.direccion.isNotBlank()) {
                        Text(
                            text = sugerencia.direccion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (terminoBusqueda && !buscando && sugerencias.isEmpty() && !elegido) {
            Text(
                text = stringResource(R.string.gestion_sin_resultados),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}