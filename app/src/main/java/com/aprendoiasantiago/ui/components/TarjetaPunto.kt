package com.aprendoiasantiago.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
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
            Text(
                text = punto.titulo,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "+${punto.puntosRecompensa} Puntos",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}