
package com.aprendoiasantiago.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.viewmodel.PuntosViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

private val VerdeVisitado = Color.rgb(46, 160, 67)
private val RojoPendiente = Color.rgb(220, 53, 69)

@Composable
fun MapaScreen(
    viewModel: PuntosViewModel,
    onVerDetalle: (Int) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var permisoUbicacion by remember {
        mutableStateOf(tienePermisoUbicacion(context))
    }

    var ubicacionUsuario by remember {
        mutableStateOf<GeoPoint?>(null)
    }

    var puntoSeleccionado by remember {
        mutableStateOf<PuntoInteres?>(null)
    }

    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        permisoUbicacion =
            resultado[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    resultado[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!permisoUbicacion) {
            permisosLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LeyendaPunto(
                color = ComposeColor(VerdeVisitado),
                texto = "Punto visitado"
            )

            LeyendaPunto(
                color = ComposeColor(RojoPendiente),
                texto = "Punto pendiente"
            )
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { ctx ->
                crearMapa(ctx)
            },
            update = { mapa ->
                actualizarMarcadores(
                    mapa = mapa,
                    puntos = uiState.puntos,
                    alSeleccionar = { punto ->
                        puntoSeleccionado = punto
                    }
                )

                actualizarOverlayUbicacion(
                    mapa = mapa,
                    habilitado = permisoUbicacion,
                    alRecibirUbicacion = { ubicacion ->
                        ubicacionUsuario = ubicacion
                    }
                )

                mapa.invalidate()
            }
        )

        OutlinedButton(
            onClick = {
                val mapa = ubicacionUsuario
                if (mapa != null) {
                    // El centrado también se realiza desde el overlay GPS.
                    // Este botón informa que ya hay una ubicación disponible.
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            enabled = permisoUbicacion
        ) {
            Text(
                if (ubicacionUsuario == null) {
                    "Buscando ubicación GPS..."
                } else {
                    "Ubicación detectada"
                }
            )
        }
    }

    val punto = puntoSeleccionado

    if (punto != null) {
        val distanciaMetros = remember(punto, ubicacionUsuario) {
            calcularDistanciaMetros(punto, ubicacionUsuario)
        }

        AlertDialog(
            onDismissRequest = {
                puntoSeleccionado = null
            },
            title = {
                Text(punto.titulo)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(punto.conceptoIA)

                    Text(
                        if (punto.fueVisitado) {
                            "Estado: visitado"
                        } else {
                            "Estado: pendiente"
                        }
                    )

                    Text("Recompensa: ${punto.puntosRecompensa} puntos")

                    Text(
                        distanciaMetros?.let { distancia ->
                            if (distancia < 1000) {
                                "Distancia: ${distancia.roundToInt()} metros"
                            } else {
                                "Distancia: ${
                                    String.format(
                                        Locale.getDefault(),
                                        "%.2f",
                                        distancia / 1000.0
                                    )
                                } km"
                            }
                        } ?: "Distancia: esperando ubicación GPS"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        puntoSeleccionado = null
                        onVerDetalle(punto.id)
                    }
                ) {
                    Text("Ver detalle")
                }
            },
            dismissButton = {
                TextButton(
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
private fun LeyendaPunto(
    color: ComposeColor,
    texto: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(13.dp)
                .background(color, CircleShape)
        )
        Text(texto, style = MaterialTheme.typography.bodySmall)
    }
}

private fun tienePermisoUbicacion(context: Context): Boolean {
    val fino = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val aproximado = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fino || aproximado
}

private fun crearMapa(context: Context): MapView {
    val directorioBase = File(context.filesDir, "osmdroid")
    val directorioTiles = File(directorioBase, "tiles")

    directorioBase.mkdirs()
    directorioTiles.mkdirs()

    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        osmdroidBasePath = directorioBase
        osmdroidTileCache = directorioTiles
        load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
    }

    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(14.5)
        controller.setCenter(GeoPoint(-33.4429, -70.6539))
    }
}

private fun actualizarMarcadores(
    mapa: MapView,
    puntos: List<PuntoInteres>,
    alSeleccionar: (PuntoInteres) -> Unit
) {
    mapa.overlays.removeAll { it is Marker }

    puntos.forEach { punto ->
        val marcador = Marker(mapa).apply {
            position = GeoPoint(punto.latitud, punto.longitud)
            title = punto.titulo
            snippet = if (punto.fueVisitado) {
                "Punto visitado"
            } else {
                "Punto pendiente"
            }

            icon = crearIconoMarcador(
                if (punto.fueVisitado) VerdeVisitado else RojoPendiente
            )

            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            setOnMarkerClickListener { _, _ ->
                alSeleccionar(punto)
                true
            }
        }

        mapa.overlays.add(marcador)
    }

    mapa.invalidate()
}

private fun crearIconoMarcador(color: Int): android.graphics.drawable.Drawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke(4, Color.WHITE)
        setSize(48, 48)
    }
}

private fun actualizarOverlayUbicacion(
    mapa: MapView,
    habilitado: Boolean,
    alRecibirUbicacion: (GeoPoint) -> Unit
) {
    val existente = mapa.overlays
        .filterIsInstance<MyLocationNewOverlay>()
        .firstOrNull()

    if (!habilitado) {
        existente?.disableMyLocation()
        mapa.overlays.removeAll { it is MyLocationNewOverlay }
        return
    }

    if (existente != null) return

    val overlay = MyLocationNewOverlay(
        GpsMyLocationProvider(mapa.context),
        mapa
    )

    overlay.enableMyLocation()

    overlay.runOnFirstFix {
        val ubicacion = overlay.myLocation

        if (ubicacion != null) {
            mapa.post {
                mapa.controller.animateTo(
                    GeoPoint(ubicacion.latitude, ubicacion.longitude)
                )

                alRecibirUbicacion(
                    GeoPoint(ubicacion.latitude, ubicacion.longitude)
                )
            }
        }
    }

    mapa.overlays.add(overlay)
    mapa.invalidate()
}

private fun calcularDistanciaMetros(
    punto: PuntoInteres,
    ubicacionUsuario: GeoPoint?
): Float? {
    if (ubicacionUsuario == null) return null

    val resultado = FloatArray(1)

    Location.distanceBetween(
        ubicacionUsuario.latitude,
        ubicacionUsuario.longitude,
        punto.latitud,
        punto.longitud,
        resultado
    )

    return resultado[0]
}
