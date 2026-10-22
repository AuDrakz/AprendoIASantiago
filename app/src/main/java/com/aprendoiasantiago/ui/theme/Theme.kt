package com.aprendoiasantiago.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AzulCielo,
    onPrimary = AzulMarino,
    primaryContainer = Color(0xFF16408F),
    onPrimaryContainer = Color(0xFFDCE8FF),
    secondary = Color(0xFFB69CFF),
    onSecondary = Color(0xFF1E0A5C),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF3B2000),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE6ECF8),
    surface = Color(0xFF131C30),
    onSurface = Color(0xFFE6ECF8),
    surfaceVariant = Color(0xFF23304D),
    onSurfaceVariant = Color(0xFFC3CCE0)
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8FF),
    onPrimaryContainer = AzulMarino,
    secondary = Violeta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9E0FF),
    onSecondaryContainer = Color(0xFF2A0F8F),
    tertiary = Naranja,
    onTertiary = Color.White,
    background = FondoClaro,
    onBackground = AzulMarino,
    surface = Color.White,
    onSurface = AzulMarino,
    surfaceVariant = Color(0xFFE6ECF8),
    onSurfaceVariant = Color(0xFF44506B)
)

private val HighContrastColorScheme = lightColorScheme(
    primary = AltoContrastePrimario,
    onPrimary = Color.White,
    primaryContainer = AltoContrastePrimarioContenedor,
    onPrimaryContainer = AltoContrasteTexto,
    secondary = AltoContrasteSecundario,
    onSecondary = Color.White,
    secondaryContainer = AltoContrasteSuperficieVariante,
    onSecondaryContainer = AltoContrasteTexto,
    tertiary = AltoContrasteTerciario,
    onTertiary = Color.White,
    background = AltoContrasteFondo,
    onBackground = AltoContrasteTexto,
    surface = AltoContrasteFondo,
    onSurface = AltoContrasteTexto,
    surfaceVariant = AltoContrasteSuperficieVariante,
    onSurfaceVariant = AltoContrasteTexto,
    outline = AltoContrasteTexto,
    outlineVariant = AltoContrasteSecundario,
    error = AltoContrasteError,
    onError = Color.White
)

@Composable
fun AprendoIASantiagoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Si es true, se ignoran el modo oscuro y la paleta de marca
    altoContraste: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        altoContraste -> HighContrastColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Íconos de las barras del sistema legibles según el fondo efectivo
    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = (vista.context as Activity).window
            val controlador = WindowCompat.getInsetsController(ventana, vista)
            val fondoClaro = altoContraste || !darkTheme
            controlador.isAppearanceLightStatusBars = fondoClaro
            controlador.isAppearanceLightNavigationBars = fondoClaro
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}