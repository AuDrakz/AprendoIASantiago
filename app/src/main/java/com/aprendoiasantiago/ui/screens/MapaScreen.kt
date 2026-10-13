package com.aprendoiasantiago.ui.screens

import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.PuntoInteres
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapaScreen(
    puntos: List<PuntoInteres>,
    userLocation: Location?,
    onNavigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current

    // Configurar osmdroid con caché offline
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().cacheMapTileCount = 15
        Configuration.getInstance().tileFileSystemCacheMaxBytes = 50L * 1024 * 1024
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            // 1. Centrar el mapa en la posición del usuario
            userLocation?.let { loc ->
                val userPoint = GeoPoint(loc.latitude, loc.longitude)
                mapView.controller.setZoom(16.5)
                mapView.controller.setCenter(userPoint)

                val userMarker = Marker(mapView).apply {
                    position = userPoint
                    title = "Mi Ubicación"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(userMarker)
            }

            // 2. Marcadores diferenciados con dialogo y distancia
            puntos.forEach { punto ->
                val puntoPoint = GeoPoint(punto.latitud, punto.longitud)

                // Cálculo de distancia
                val distanciaTexto = userLocation?.let { loc ->
                    val pLoc = Location("").apply {
                        latitude = punto.latitud
                        longitude = punto.longitud
                    }
                    val dist = loc.distanceTo(pLoc)
                    context.getString(R.string.distance_format, dist)
                } ?: ""

                val marker = Marker(mapView).apply {
                    position = puntoPoint
                    title = punto.nombre
                    snippet = "$distanciaTexto\n${punto.descripcion}"

                    // Iconos diferenciados por estado (Visitado vs Pendiente)
                    icon = if (punto.esVisitado) {
                        context.getDrawable(R.drawable.ic_launcher_foreground) // Reemplazar con ícono verde/visitado
                    } else {
                        context.getDrawable(R.drawable.ic_launcher_background) // Reemplazar con ícono rojo/pendiente
                    }

                    // Redirección al detalle al pulsar la ventana emergente
                    setOnMarkerClickListener { m, _ ->
                        m.showInfoWindow()
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}