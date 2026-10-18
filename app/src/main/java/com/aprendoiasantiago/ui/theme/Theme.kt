package com.aprendoiasantiago.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
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
    // Si es true, se ignoran el modo oscuro y los colores dinámicos
    altoContraste: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        altoContraste -> HighContrastColorScheme

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

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