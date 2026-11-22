package com.aprendoiasantiago.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aprendoiasantiago.R
import com.aprendoiasantiago.data.remote.EstadoRed

/**
 * Avisa el estado de la conexión con el servidor: barra de progreso mientras sincroniza
 * y un mensaje con botón "Reintentar" si falló. Si todo va bien, no ocupa espacio.
 */
@Composable
fun BannerEstadoRed(
    estado: EstadoRed,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (estado) {
        is EstadoRed.Cargando -> Column(modifier = modifier.fillMaxWidth()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                text = stringResource(R.string.red_sincronizando),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }

        is EstadoRed.Error -> Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(estado.tipo.mensaje),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onReintentar) {
                    Text(stringResource(R.string.red_reintentar))
                }
            }
        }

        else -> Unit
    }
}