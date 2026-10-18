package com.aprendoiasantiago.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aprendoiasantiago.R
import com.aprendoiasantiago.data.local.UserPreferencesRepository
import com.aprendoiasantiago.ui.components.IconoEscanerQr
import com.aprendoiasantiago.ui.screens.DetalleVideoScreen
import com.aprendoiasantiago.ui.screens.EditarPerfilScreen
import com.aprendoiasantiago.ui.screens.ListaPuntosScreen
import com.aprendoiasantiago.ui.screens.MapaScreen
import com.aprendoiasantiago.ui.screens.PerfilRankingScreen
import com.aprendoiasantiago.ui.screens.QrScannerScreen
import com.aprendoiasantiago.ui.screens.RegistroScreen
import com.aprendoiasantiago.viewmodel.EscanerViewModel
import com.aprendoiasantiago.viewmodel.PerfilViewModel
import com.aprendoiasantiago.viewmodel.PuntosViewModel

private data class ItemBarra(
    val screen: Screen,
    @param: StringRes val etiqueta: Int,
    val icono: ImageVector
)

private val itemsBarra = listOf(
    ItemBarra(
        Screen.ListaPuntos,
        R.string.nav_puntos,
        Icons.AutoMirrored.Filled.List
    ),
    ItemBarra(
        Screen.Mapa,
        R.string.nav_mapa,
        Icons.Filled.Place
    ),
    ItemBarra(
        Screen.ScannerQR,
        R.string.nav_escaner,
        IconoEscanerQr
    ),
    ItemBarra(
        Screen.Perfil,
        R.string.nav_perfil,
        Icons.Filled.Person
    )
)

@Composable
fun AppNavigation(
    puntosViewModel: PuntosViewModel,
    perfilViewModel: PerfilViewModel,
    escanerViewModel: EscanerViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    windowSizeClass: WindowSizeClass
) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val irAPestana: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    val navHost: @Composable (Modifier) -> Unit = { modifier ->
        AppNavHost(
            navController = navController,
            puntosViewModel = puntosViewModel,
            perfilViewModel = perfilViewModel,
            escanerViewModel = escanerViewModel,
            userPreferencesRepository = userPreferencesRepository,
            modifier = modifier
        )
    }

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        Row(Modifier.fillMaxSize()) {
            NavigationRail {
                itemsBarra.forEach { item ->
                    NavigationRailItem(
                        selected = rutaActual == item.screen.route,
                        onClick = { irAPestana(item.screen.route) },
                        icon = {
                            Icon(
                                item.icono,
                                contentDescription = stringResource(item.etiqueta)
                            )
                        },
                        label = { Text(stringResource(item.etiqueta)) }
                    )
                }
            }
            navHost(Modifier.weight(1f))
        }
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar {
                    itemsBarra.forEach { item ->
                        NavigationBarItem(
                            selected = rutaActual == item.screen.route,
                            onClick = { irAPestana(item.screen.route) },
                            icon = {
                                Icon(
                                    item.icono,
                                    contentDescription = stringResource(item.etiqueta)
                                )
                            },
                            label = { Text(stringResource(item.etiqueta)) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            navHost(
                Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    puntosViewModel: PuntosViewModel,
    perfilViewModel: PerfilViewModel,
    escanerViewModel: EscanerViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ListaPuntos.route,
        modifier = modifier
    ) {
        composable(Screen.ListaPuntos.route) {
            ListaPuntosScreen(
                viewModel = puntosViewModel,
                onIrAEscaner = {
                    navController.navigate(Screen.ScannerQR.route)
                }
            )
        }

        composable(Screen.Mapa.route) {
            MapaScreen(puntosViewModel)
        }

        composable(Screen.ScannerQR.route) {
            QrScannerScreen(
                viewModel = escanerViewModel,
                onVerVideo = { puntoId ->
                    navController.navigate(Screen.DetalleVideo.crearRuta(puntoId))
                }
            )
        }

        composable(
            route = Screen.DetalleVideo.route,
            arguments = listOf(
                navArgument(Screen.DetalleVideo.ARG_PUNTO_ID) {
                    type = NavType.IntType
                }
            )
        ) { entrada ->
            val puntoId = entrada.arguments?.getInt(Screen.DetalleVideo.ARG_PUNTO_ID)
            val uiState by puntosViewModel.uiState.collectAsStateWithLifecycle()

            DetalleVideoScreen(
                punto = uiState.puntos.firstOrNull { it.id == puntoId },
                onVolver = { navController.popBackStack() },
                onSelfie = { ruta ->
                    puntoId?.let { puntosViewModel.guardarSelfie(it, ruta) }
                }
            )
        }

        composable(Screen.Perfil.route) {
            PerfilRankingScreen(
                viewModel = perfilViewModel,
                userPreferencesRepository = userPreferencesRepository,
                onEditarPerfil = { navController.navigate(Screen.EditarPerfil.route) },
                onRegistro = { navController.navigate(Screen.Registro.route) }
            )
        }

        composable(Screen.EditarPerfil.route) {
            EditarPerfilScreen(
                viewModel = perfilViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Screen.Registro.route) {
            RegistroScreen(
                viewModel = perfilViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}