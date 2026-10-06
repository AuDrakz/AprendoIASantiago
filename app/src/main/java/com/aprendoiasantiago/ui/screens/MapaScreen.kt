package com.aprendoiasantiago.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.viewmodel.AprendoIaViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    viewModel: AprendoIaViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var ubicacionPermitida by remember {
        mutableStateOf(
            tienePermisoUbicacion(context)
        )
    }

    var puntoSeleccionado by remember {
        mutableStateOf<PuntoInteres?>(null)
    }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permisos ->

            ubicacionPermitida =
                permisos[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||
                        permisos[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true
        }

    LaunchedEffect(Unit) {

        if (!ubicacionPermitida) {

            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mapa de puntos")
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            MapaOsmdroid(
                puntos = uiState.puntos,
                mostrarUbicacion = ubicacionPermitida,
                onPuntoClick = {
                    puntoSeleccionado = it
                }
            )

            if (!ubicacionPermitida) {

                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    tonalElevation = 6.dp,
                    shape = MaterialTheme.shapes.medium
                ) {

                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        Text(
                            "Activa el permiso de ubicación " +
                                    "para mostrar tu posición."
                        )

                        Button(
                            onClick = {
                                launcher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        ) {
                            Text("Permitir ubicación")
                        }
                    }
                }
            }
        }
    }

    puntoSeleccionado?.let { punto ->

        AlertDialog(
            onDismissRequest = {
                puntoSeleccionado = null
            },

            title = {
                Text(punto.titulo)
            },

            text = {
                Text(
                    "Concepto de IA: ${punto.conceptoIA}\n" +
                            "Recompensa: ${punto.puntosRecompensa} puntos"
                )
            },

            confirmButton = {
                Button(
                    onClick = {
                        puntoSeleccionado = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun MapaOsmdroid(
    puntos: List<PuntoInteres>,
    mostrarUbicacion: Boolean,
    onPuntoClick: (PuntoInteres) -> Unit
) {
    val context = LocalContext.current

    val mapView = remember {
        crearMapa(context)
    }

    var locationOverlay
            by remember {
                mutableStateOf<MyLocationNewOverlay?>(null)
            }

    LaunchedEffect(puntos) {

        mapView.overlays.removeAll {
            it is Marker
        }

        puntos.forEach { punto ->

            val marker =
                Marker(mapView).apply {

                    position = GeoPoint(
                        punto.latitud,
                        punto.longitud
                    )

                    title = punto.titulo

                    snippet = punto.conceptoIA

                    setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )

                    setOnMarkerClickListener { _, _ ->

                        onPuntoClick(punto)

                        true
                    }
                }

            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    LaunchedEffect(mostrarUbicacion) {

        if (
            mostrarUbicacion &&
            locationOverlay == null
        ) {

            val overlay =
                MyLocationNewOverlay(
                    GpsMyLocationProvider(context),
                    mapView
                )

            overlay.enableMyLocation()

            mapView.overlays.add(overlay)

            locationOverlay = overlay

            mapView.invalidate()

        } else if (!mostrarUbicacion) {

            locationOverlay?.disableMyLocation()

            locationOverlay?.let {
                mapView.overlays.remove(it)
            }

            locationOverlay = null

            mapView.invalidate()
        }
    }

    AndroidView(
        factory = {
            mapView
        },
        modifier = Modifier.fillMaxSize(),
        update = {
            it.invalidate()
        }
    )

    DisposableEffect(Unit) {

        onDispose {

            locationOverlay?.disableMyLocation()

            mapView.onDetach()
        }
    }
}

private fun crearMapa(
    context: Context
): MapView {

    Configuration
        .getInstance()
        .userAgentValue = context.packageName

    return MapView(context).apply {

        setTileSource(
            TileSourceFactory.MAPNIK
        )

        setMultiTouchControls(true)

        controller.setZoom(14.5)

        controller.setCenter(
            GeoPoint(
                -33.4429,
                -70.6539
            )
        )
    }
}

private fun tienePermisoUbicacion(
    context: Context
): Boolean {

    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}