package com.aprendoiasantiago.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File

/** Imagen circular. Si no hay foto, muestra un ícono por defecto. */
@Composable
fun ImagenInteligente(
    ruta: String?,
    modifier: Modifier = Modifier,
    tamano: Dp = 96.dp,
    descripcion: String? = null
) {
    val base = modifier
        .size(tamano)
        .clip(CircleShape)

    if (ruta.isNullOrBlank()) {
        Box(
            modifier = base.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = descripcion,
                modifier = Modifier.size(tamano / 2),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        AsyncImage(
            model = File(ruta),
            contentDescription = descripcion,
            modifier = base,
            contentScale = ContentScale.Crop
        )
    }
}