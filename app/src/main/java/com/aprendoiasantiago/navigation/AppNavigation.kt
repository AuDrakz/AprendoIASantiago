package com.aprendoiasantiago.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aprendoiasantiago.ui.screens.DetalleVideoScreen
import com.aprendoiasantiago.ui.screens.ListaPuntosScreen
import com.aprendoiasantiago.ui.screens.PantallaPendiente
import com.aprendoiasantiago.ui.screens.PerfilRankingScreen
import com.aprendoiasantiago.ui.screens.QrScannerScreen
import com.aprendoiasantiago.viewmodel.AprendoIaViewModel

/** Un botón de la barra inferior: a dónde va, su texto y su icono. */
private data class ItemBarra(
    val screen: Screen,
    val etiqueta: String,
    val icono: ImageVector
)

private val itemsBarra = listOf(
    ItemBarra(Screen.ListaPuntos, "Puntos", Icons.AutoMirrored.Filled.List),
    ItemBarra(Screen.Mapa, "Mapa", Icons.Filled.Place),
    ItemBarra(Screen.ScannerQR, "Escáner", Icons.Filled.Search),
    ItemBarra(Screen.Perfil, "Perfil", Icons.Filled.Person)
)

/**
 * Navegación central de la app: barra inferior + NavHost.
 * Recibe UN solo ViewModel y lo comparte con las pantallas.
 */
@Composable
fun AppNavigation(viewModel: AprendoIaViewModel) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    // Cambia de pestaña sin apilar pantallas repetidas.
    // Lo usan la barra inferior y el botón "Escanear QR" de las tarjetas.
    val irAPestana: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                itemsBarra.forEach { item ->
                    NavigationBarItem(
                        selected = rutaActual == item.screen.route,
                        onClick = { irAPestana(item.screen.route) },
                        icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                        label = { Text(item.etiqueta) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ListaPuntos.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.ListaPuntos.route) {
                ListaPuntosScreen(
                    viewModel = viewModel,
                    onIrAEscaner = { irAPestana(Screen.ScannerQR.route) }
                )
            }
            composable(Screen.Mapa.route) {
                PantallaPendiente("Mapa", "Aquí irá el mapa (Fase 5)")
            }
            composable(Screen.ScannerQR.route) {
                QrScannerScreen(
                    viewModel = viewModel,
                    onVerVideo = { puntoId ->
                        navController.navigate(Screen.DetalleVideo.crearRuta(puntoId))
                    }
                )
            }
            composable(
                route = Screen.DetalleVideo.route,
                arguments = listOf(
                    navArgument(Screen.DetalleVideo.ARG_PUNTO_ID) { type = NavType.IntType }
                )
            ) { entrada ->
                val puntoId = entrada.arguments?.getInt(Screen.DetalleVideo.ARG_PUNTO_ID)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                DetalleVideoScreen(
                    punto = uiState.puntos.firstOrNull { it.id == puntoId },
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Screen.Perfil.route) {
                PerfilRankingScreen(viewModel)
            }
        }
    }
}