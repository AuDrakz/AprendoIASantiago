package com.aprendoiasantiago.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Colores de los puntos del mapa; cambian con el modo daltonismo. */
@Immutable
data class ColoresEstado(
    val visitado: Color,
    val noDescubierto: Color,
    val admin: Color
)

val LocalColoresEstado = staticCompositionLocalOf {
    ColoresEstado(EstadoVisitado, EstadoNoDescubierto, EstadoAdmin)
}

private val ColoresNormales = ColoresEstado(EstadoVisitado, EstadoNoDescubierto, EstadoAdmin)
private val ColoresDaltonismo = ColoresEstado(DaltonCeleste, EstadoNoDescubierto, DaltonPurpura)

private val DarkColorScheme = darkColorScheme(
    primary = NeonCian,
    onPrimary = NeonFondo,
    primaryContainer = Color(0xFF123A5C),
    onPrimaryContainer = Color(0xFFD6F6FF),
    secondary = NeonVioleta,
    onSecondary = Color(0xFF1E0A5C),
    secondaryContainer = Color(0xFF3B2A7A),
    onSecondaryContainer = Color(0xFFE9E0FF),
    tertiary = NeonLima,
    onTertiary = Color(0xFF1F2E00),
    background = NeonFondo,
    onBackground = Color(0xFFE6ECF8),
    surface = NeonSuperficie,
    onSurface = Color(0xFFE6ECF8),
    surfaceVariant = NeonSuperficieVariante,
    onSurfaceVariant = Color(0xFFC3CCE0),
    outline = Color(0xFF3A4A82),
    outlineVariant = Color(0xFF26325F)
)

private val DarkDaltonismo = DarkColorScheme.copy(
    primary = DaltonCeleste,
    onPrimary = Color(0xFF00121F),
    primaryContainer = Color(0xFF0B3A5E),
    onPrimaryContainer = Color(0xFFD8EEFF),
    secondary = DaltonNaranja,
    onSecondary = Color(0xFF2B1B00),
    secondaryContainer = Color(0xFF5C3F00),
    onSecondaryContainer = Color(0xFFFFE9BF),
    tertiary = DaltonPurpura,
    onTertiary = Color(0xFF2B0F20),
    error = Color(0xFFD55E00),
    outline = Color(0xFF4A5A80)
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

private val LightDaltonismo = LightColorScheme.copy(
    primary = DaltonAzul,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFE8F7),
    onPrimaryContainer = Color(0xFF00263D),
    secondary = DaltonNaranja,
    onSecondary = Color(0xFF2B1B00),
    secondaryContainer = Color(0xFFFFE9BF),
    onSecondaryContainer = Color(0xFF3B2A00),
    tertiary = DaltonPurpura,
    onTertiary = Color.White,
    error = Color(0xFFD55E00)
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
    // Si es true, usa colores azul/naranja y no depende solo del color
    daltonismo: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        altoContraste -> HighContrastColorScheme
        darkTheme && daltonismo -> DarkDaltonismo
        darkTheme -> DarkColorScheme
        daltonismo -> LightDaltonismo
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

    CompositionLocalProvider(
        LocalColoresEstado provides if (daltonismo) ColoresDaltonismo else ColoresNormales
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}