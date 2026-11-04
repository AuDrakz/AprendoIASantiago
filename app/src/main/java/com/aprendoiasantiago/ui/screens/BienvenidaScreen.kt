package com.aprendoiasantiago.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aprendoiasantiago.R
import com.aprendoiasantiago.ui.components.FondoNeon
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// Posiciones (0 a 1) de los nodos de la red de fondo
private val NODOS = listOf(
    Offset(0.10f, 0.10f), Offset(0.85f, 0.07f), Offset(0.30f, 0.28f), Offset(0.92f, 0.33f),
    Offset(0.12f, 0.58f), Offset(0.78f, 0.60f), Offset(0.40f, 0.82f), Offset(0.90f, 0.90f)
)

@Composable
fun BienvenidaScreen(
    onComenzar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Animaciones continuas
    val infinito = rememberInfiniteTransition(label = "bienvenida")
    val giro by infinito.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "giro"
    )
    val pulso by infinito.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulso"
    )
    val deriva by infinito.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "deriva"
    )
    val brillo by infinito.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "brillo"
    )

    FondoNeon {
        // Red de nodos animada (tema inteligencia artificial)
        Canvas(Modifier.fillMaxSize()) {
            val alcance = size.width * 0.55f
            val puntos = NODOS.mapIndexed { i, base ->
                Offset(
                    base.x * size.width + sin(deriva + i) * 14.dp.toPx(),
                    base.y * size.height + cos(deriva + i * 1.7f) * 14.dp.toPx()
                )
            }
            for (a in puntos.indices) {
                for (b in a + 1 until puntos.size) {
                    val distancia = (puntos[a] - puntos[b]).getDistance()
                    if (distancia < alcance) {
                        drawLine(
                            color = colores.primary.copy(alpha = 0.28f * (1f - distancia / alcance)),
                            start = puntos[a],
                            end = puntos[b],
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }
            puntos.forEach {
                drawCircle(colores.secondary.copy(alpha = 0.85f), radius = 4.dp.toPx(), center = it)
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 5 },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LogoNeon(giro = { giro }, pulso = { pulso })

                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.bienvenida_titulo),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        brush = Brush.linearGradient(listOf(colores.primary, colores.secondary))
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.bienvenida_descripcion),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colores.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                ItemBienvenida(Icons.Filled.PlayArrow, R.string.bienvenida_item_videos, colores.tertiary, 400)
                ItemBienvenida(Icons.Filled.Place, R.string.bienvenida_item_lugares, colores.secondary, 600)
                ItemBienvenida(Icons.Filled.Star, R.string.bienvenida_item_puntos, colores.primary, 800)

                Spacer(Modifier.height(36.dp))

                BotonComenzar(brillo = { brillo }, onClick = onComenzar)
            }
        }
    }
}

/** Logo con anillo de colores que gira y un resplandor que late. */
@Composable
private fun LogoNeon(giro: () -> Float, pulso: () -> Float) {
    val colores = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .size(170.dp)
            .graphicsLayer {
                scaleX = pulso()
                scaleY = pulso()
            }
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(colores.primary.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    radius = size.minDimension * 0.95f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = giro() }
        ) {
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(colores.primary, colores.secondary, colores.tertiary, colores.primary)
                ),
                radius = size.minDimension / 2f - 4.dp.toPx(),
                style = Stroke(width = 5.dp.toPx())
            )
        }
        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(colores.surface)
                .border(1.dp, colores.primary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = colores.primary
            )
        }
    }
}

/** Botón con degradado y un destello que lo recorre. */
@Composable
private fun BotonComenzar(brillo: () -> Float, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(30.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(16.dp, forma, ambientColor = colores.primary, spotColor = colores.primary)
            .clip(forma)
            .background(Brush.horizontalGradient(listOf(colores.primary, colores.secondary)))
            .drawWithContent {
                drawContent()
                val x = brillo() * size.width
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        startX = x - 90.dp.toPx(),
                        endX = x + 90.dp.toPx()
                    )
                )
            }
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.bienvenida_comenzar),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colores.onPrimary
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = colores.onPrimary
        )
    }
}

/** Fila que entra deslizándose desde la derecha, con un pequeño retraso. */
@Composable
private fun ItemBienvenida(
    icono: ImageVector,
    @StringRes texto: Int,
    color: Color,
    retrasoMs: Int
) {
    val colores = MaterialTheme.colorScheme
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(retrasoMs.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInHorizontally(tween(500)) { it / 3 }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colores.surface.copy(alpha = 0.85f))
                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colores.background
                )
            }
            Text(
                text = stringResource(texto),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colores.onSurface,
                modifier = Modifier.padding(start = 14.dp)
            )
        }
    }
}