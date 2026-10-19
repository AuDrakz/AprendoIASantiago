package com.aprendoiasantiago.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.aprendoiasantiago.model.PuntoInteres

@Composable
fun DetalleVideoScreen(
    punto: PuntoInteres?,
    onVolver: () -> Unit,
    onSelfie: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onVolver) {
            Text("Volver")
        }

        if (punto == null) {
            Text(
                text = "No se encontró el punto seleccionado.",
                style = MaterialTheme.typography.bodyLarge
            )
            return@Column
        }

        Text(
            text = punto.titulo,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = punto.conceptoIA,
            style = MaterialTheme.typography.bodyLarge
        )

        Text("Recompensa: ${punto.puntosRecompensa} puntos")

        Text(
            text = if (punto.fueVisitado) {
                "Estado: punto visitado"
            } else {
                "Estado: punto pendiente"
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Video educativo",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                ReproductorVideo(
                    url = punto.urlVideo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                )
            }
        }

        if (punto.fueVisitado) {
            Button(
                onClick = onSelfie,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver o tomar selfie")
            }
        }
    }
}

@OptIn(markerClass = [UnstableApi::class])
@Composable
private fun ReproductorVideo(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var cargando by remember(url) {
        mutableStateOf(url.isNotBlank())
    }

    var errorVideo by remember(url) {
        mutableStateOf<String?>(null)
    }

    val reproductor = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false

            addListener(object : Player.Listener {

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {
                    cargando =
                        playbackState == Player.STATE_BUFFERING ||
                                playbackState == Player.STATE_IDLE && url.isNotBlank()

                    if (playbackState == Player.STATE_READY) {
                        cargando = false
                        errorVideo = null
                    }

                    if (playbackState == Player.STATE_ENDED) {
                        cargando = false
                    }
                }

                override fun onPlayerError(
                    error: PlaybackException
                ) {
                    cargando = false
                    errorVideo =
                        "No se pudo reproducir el video. " +
                                "Comprueba tu conexión o la dirección del video."
                }
            })

            if (url.isNotBlank()) {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
            } else {
                cargando = false
                errorVideo = "Este punto no tiene una dirección de video."
            }
        }
    }

    DisposableEffect(reproductor) {
        onDispose {
            reproductor.release()
        }
    }

    Column(modifier = modifier) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = reproductor
                    useController = true
                    setShowBuffering(
                        PlayerView.SHOW_BUFFERING_WHEN_PLAYING
                    )
                }
            },
            update = { vista ->
                vista.player = reproductor
            }
        )

        if (cargando && errorVideo == null) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        errorVideo?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
