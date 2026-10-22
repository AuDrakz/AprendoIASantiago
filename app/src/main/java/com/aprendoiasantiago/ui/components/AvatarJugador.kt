package com.aprendoiasantiago.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aprendoiasantiago.R

private class EstiloAvatar(val icono: ImageVector, val color: Color)

private val estilos = listOf(
    EstiloAvatar(Icons.Filled.Face, Color(0xFF1E88E5)),
    EstiloAvatar(Icons.Filled.Star, Color(0xFFFB8C00)),
    EstiloAvatar(Icons.Filled.Favorite, Color(0xFFE53935)),
    EstiloAvatar(Icons.Filled.ThumbUp, Color(0xFF43A047)),
    EstiloAvatar(Icons.Filled.Person, Color(0xFF8E24AA)),
    EstiloAvatar(Icons.Filled.Settings, Color(0xFF546E7A))
)

/** Cantidad de avatares disponibles (debe coincidir con la lista de arriba). */
const val TOTAL_AVATARES = 6

/** Avatar circular: la foto del jugador si la tiene; si no, un ícono con color propio. */
@Composable
fun AvatarJugador(
    indice: Int,
    modifier: Modifier = Modifier,
    fotoRuta: String? = null,
    tamano: Dp = 56.dp,
    seleccionado: Boolean = false
) {
    val descripcion = stringResource(R.string.avatar_descripcion)

    if (!fotoRuta.isNullOrBlank()) {
        ImagenInteligente(
            ruta = fotoRuta,
            modifier = modifier,
            tamano = tamano,
            descripcion = descripcion
        )
        return
    }

    val estilo = estilos[indice.mod(estilos.size)]
    val borde = if (seleccionado) {
        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(estilo.color)
            .then(borde),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = estilo.icono,
            contentDescription = descripcion,
            tint = Color.White,
            modifier = Modifier.size(tamano / 2)
        )
    }
}