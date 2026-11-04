package com.aprendoiasantiago.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aprendoiasantiago.R
import com.aprendoiasantiago.ui.theme.LocalColoresEstado

/** Fondo degradado con un resplandor de color arriba. Úsalo como raíz de cada pantalla. */
@Composable
fun FondoNeon(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colores = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colores.background, colores.surface)))
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colores.primary.copy(alpha = 0.22f),
                            colores.background.copy(alpha = 0f)
                        ),
                        center = Offset(size.width * 0.8f, 0f),
                        radius = size.width * 0.9f
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(size.width * 0.8f, 0f)
                )
            },
        content = content
    )
}

/** Insignia "Descubierto" (check) o "No descubierto" (gris + candado). */
@Composable
fun ChipEstado(visitado: Boolean, modifier: Modifier = Modifier) {
    val estados = LocalColoresEstado.current
    val color = if (visitado) estados.visitado else estados.noDescubierto
    val forma = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .clip(forma)
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.8f), forma)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (visitado) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(
                if (visitado) R.string.tarjeta_visitado else R.string.tarjeta_pendiente
            ),
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

data class ItemBarraFlotante(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector,
    /** El botón destacado (QR) se dibuja como círculo con degradado. */
    val destacado: Boolean = false
)

/** Barra inferior en forma de píldora; el ítem activo se agranda con rebote. */
@Composable
fun BarraInferiorFlotante(
    items: List<ItemBarraFlotante>,
    rutaActual: String?,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(32.dp)

    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(12.dp, forma, ambientColor = colores.primary, spotColor = colores.primary)
            .clip(forma)
            .background(colores.surface)
            .border(1.dp, colores.primary.copy(alpha = 0.35f), forma)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            ElementoBarra(
                item = item,
                seleccionado = rutaActual == item.ruta,
                onClick = { onSeleccion(item.ruta) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ElementoBarra(
    item: ItemBarraFlotante,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    val escala by animateFloatAsState(
        targetValue = if (seleccionado) 1.18f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "escalaItem"
    )
    val colorIcono by animateColorAsState(
        targetValue = if (seleccionado) colores.primary else colores.onSurfaceVariant,
        label = "colorItem"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .graphicsLayer { scaleX = escala; scaleY = escala },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (item.destacado) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(10.dp, CircleShape, ambientColor = colores.primary, spotColor = colores.primary)
                    .background(
                        Brush.linearGradient(listOf(colores.primary, colores.secondary)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icono, contentDescription = item.etiqueta, tint = colores.onPrimary)
            }
        } else {
            Icon(item.icono, contentDescription = item.etiqueta, tint = colorIcono)
            Text(
                text = item.etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = colorIcono,
                maxLines = 1
            )
        }
    }
}

/** Borde sutil de tarjeta neón. */
@Composable
fun bordeNeon(): BorderStroke =
    BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))