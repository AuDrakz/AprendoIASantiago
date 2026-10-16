package com.aprendoiasantiago.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aprendoiasantiago.R
import com.aprendoiasantiago.model.ResultadoEscaneo
import com.aprendoiasantiago.viewmodel.EscanerViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Versión "stateful": maneja el permiso de cámara y valida cada QR con el ViewModel.
 * Si el QR es exitoso, muestra la animación de +100 puntos ANTES de navegar al video.
 */
@Composable
fun QrScannerScreen(
    viewModel: EscanerViewModel,
    onVerVideo: (Int) -> Unit
) {
    val contexto = LocalContext.current
    val recursos = LocalResources.current
    val alcance = rememberCoroutineScope()

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> permisoConcedido = concedido }

    val instruccion = stringResource(R.string.escaner_instruccion)
    var mensaje by remember { mutableStateOf(instruccion) }
    var procesando by remember { mutableStateOf(false) }

    // Estado para controlar cuándo mostrar la tarjeta con la animación AnimatedVisibility
    var mostrarBannerExito by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!permisoConcedido) lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    QrScannerContent(
        permisoConcedido = permisoConcedido,
        mensaje = mensaje,
        mostrarBannerExito = mostrarBannerExito,
        onPedirPermiso = { lanzadorPermiso.launch(Manifest.permission.CAMERA) },
        onAbrirAjustes = {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", contexto.packageName, null)
            )
            contexto.startActivity(intent)
        },
        onCodigoDetectado = { codigo ->
            if (!procesando) {
                procesando = true
                viewModel.escanearCodigoQR(codigo) { resultado ->
                    when (resultado) {
                        is ResultadoEscaneo.Exito -> {
                            mensaje = recursos.getString(
                                R.string.escaner_exito,
                                resultado.punto.puntosRecompensa,
                                resultado.punto.titulo
                            )
                            // 1. Activamos el banner animado
                            mostrarBannerExito = true

                            // 2. Esperamos 1.8 segundos para que el usuario VEA el "+100 puntos" antes de pasar al video
                            alcance.launch {
                                delay(1.8.seconds)
                                mostrarBannerExito = false
                                onVerVideo(resultado.punto.id)
                                procesando = false
                            }
                        }
                        is ResultadoEscaneo.YaVisitado -> {
                            mensaje = recursos.getString(R.string.escaner_ya_visitado, resultado.punto.titulo)
                            alcance.launch {
                                delay(2.seconds)
                                procesando = false
                            }
                        }
                        ResultadoEscaneo.Invalido -> {
                            mensaje = recursos.getString(R.string.escaner_invalido)
                            alcance.launch {
                                delay(2.seconds)
                                procesando = false
                            }
                        }
                    }
                }
            }
        }
    )
}

/** Versión "stateless": dibuja la interfaz y anima el mensaje de puntos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerContent(
    permisoConcedido: Boolean,
    mensaje: String,
    mostrarBannerExito: Boolean,
    onPedirPermiso: () -> Unit,
    onAbrirAjustes: () -> Unit,
    onCodigoDetectado: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.escaner_titulo)) }) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (permisoConcedido) {
                CamaraQr(
                    onCodigoDetectado = onCodigoDetectado,
                    modifier = Modifier.fillMaxSize()
                )

                // Animación de entrada y salida para el mensaje de éxito / estado
                AnimatedVisibility(
                    visible = true, // Permanece visible para mostrar las instrucciones o avisos
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (mostrarBannerExito) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        },
                        tonalElevation = 8.dp
                    ) {
                        Text(
                            text = mensaje,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = if (mostrarBannerExito) {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                MaterialTheme.typography.bodyMedium
                            }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.escaner_permiso_texto),
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onPedirPermiso) { Text(stringResource(R.string.escaner_permiso_boton)) }
                    OutlinedButton(onClick = onAbrirAjustes) { Text(stringResource(R.string.escaner_ajustes_boton)) }
                }
            }
        }
    }
}

/** Vista previa de la cámara + lectura de QR con ML Kit. */
@Composable
private fun CamaraQr(
    onCodigoDetectado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current
    val alDetectar by rememberUpdatedState(onCodigoDetectado)

    val controlador = remember { LifecycleCameraController(contexto) }

    DisposableEffect(ciclo) {
        val escaner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
        val ejecutor = ContextCompat.getMainExecutor(contexto)

        controlador.setImageAnalysisAnalyzer(
            ejecutor,
            MlKitAnalyzer(
                listOf(escaner),
                ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ejecutor
            ) { resultado: MlKitAnalyzer.Result? ->
                val codigo = resultado?.getValue(escaner)?.firstOrNull()?.rawValue
                if (codigo != null) alDetectar(codigo)
            }
        )
        controlador.bindToLifecycle(ciclo)

        onDispose {
            controlador.clearImageAnalysisAnalyzer()
            controlador.unbind()
            escaner.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply { this.controller = controlador }
        }
    )
}