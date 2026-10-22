package com.aprendoiasantiago.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Icono de escáner QR (esquinas + bloques centrales). */
val IconoQr: ImageVector by lazy {
    ImageVector.Builder(
        name = "IconoQr",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(
            "M3,3h6v2H5v4H3V3z M15,3h6v6h-2V5h-4V3z " +
                    "M3,15h2v4h4v2H3v-6z M19,15h2v6h-6v-2h4v-4z " +
                    "M8,8h3v3H8V8z M13,8h3v3h-3V8z M8,13h3v3H8v-3z M13,13h3v3h-3v-3z"
        ),
        fill = SolidColor(Color.Black)
    ).build()
}