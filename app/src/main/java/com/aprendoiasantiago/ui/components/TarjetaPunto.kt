package com.aprendoiasantiago.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.PuntoInteres

@Composable
fun TarjetaPunto(
    punto: PuntoInteres,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fuentesInteraccion = remember { MutableInteractionSource() }
    val presionado by fuentesInteraccion.collectIsPressedAsState()

    // 1. Animación de escala al presionar
    val escala by animateFloatAsState(
        targetValue = if (presionado) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "escalaTarjeta"
    )

    // 2. Animación de elevación de la tarjeta
    val elevacion by animateDpAsState(
        targetValue = if (presionado) 8.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "elevacionTarjeta"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .clickable(
                interactionSource = fuentesInteraccion,
                indication = null
            ) {
                onClick()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = elevacion)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = punto.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                // Insignia Visitado / Pendiente usando recursos de strings
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = if (punto.fueVisitado) {
                                stringResource(R.string.tarjeta_visitado)
                            } else {
                                stringResource(R.string.tarjeta_pendiente)
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (punto.fueVisitado) Icons.Default.Check else Icons.Default.Info,
                            contentDescription = null
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Concepto de IA recuperado
            Text(
                text = stringResource(R.string.tarjeta_concepto_ia, punto.conceptoIA),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Formateado desde strings.xml (+N puntos)
            Text(
                text = stringResource(R.string.puntos_recompensa, punto.puntosRecompensa),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}