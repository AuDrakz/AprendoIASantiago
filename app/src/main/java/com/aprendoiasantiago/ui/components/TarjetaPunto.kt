package com.aprendoiasantiago.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aprendoiasantiago.model.PuntoInteres

/**
 * Tarjeta reutilizable de un punto de interés (stateless):
 * recibe datos y expone eventos, no conoce el ViewModel.
 */
@Composable
fun TarjetaPunto(
    punto: PuntoInteres,
    onEscanearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visitado = punto.fueVisitado

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (visitado) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = punto.titulo, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = punto.conceptoIA,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                BadgeEstado(visitado = visitado)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "+${punto.puntosRecompensa} puntos",
                style = MaterialTheme.typography.labelLarge
            )

            // El botón solo aparece si el punto aún no se visita
            if (!visitado) {
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = onEscanearClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Escanear QR")
                }
            }
        }
    }
}

/** Insignia "Visitado" / "Pendiente" con icono. */
@Composable
private fun BadgeEstado(visitado: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (visitado) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (visitado) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (visitado) Icons.Filled.CheckCircle else Icons.Filled.Place,
                contentDescription = null, // el texto ya describe el estado
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (visitado) "Visitado" else "Pendiente",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaPuntoPreview() {
    MaterialTheme {
        TarjetaPunto(
            punto = PuntoInteres(
                1, "Palacio de La Moneda", "Visión por Computador",
                -33.4429, -70.6539, "QR_LAMONEDA_01", 100, ""
            ),
            onEscanearClick = {}
        )
    }
}