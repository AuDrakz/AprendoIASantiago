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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aprendoiasantiago.model.ResultadoEscaneo
import com.aprendoiasantiago.viewmodel.AprendoIaViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Versión "stateful": maneja el permiso de cámara y valida cada QR
 * con el ViewModel. Si el QR es nuevo, navega al detalle del video.
 */
@Composable
fun QrScannerScreen(
    viewModel: AprendoIaViewModel,
    onVerVideo: (Int) -> Unit
) {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()

    // ¿Ya tenemos permiso de cámara?
    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    // Lanzador del cuadro "¿Permitir usar la cámara?"
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> permisoConcedido = concedido }

    var mensaje by remember { mutableStateOf("Apunta la cámara al código QR del lugar") }
    // Evita procesar el mismo QR decenas de veces por segundo
    var procesando by remember { mutableStateOf(false) }

    // Al abrir la pantalla, si falta el permiso, lo pide
    LaunchedEffect(Unit) {
        if (!permisoConcedido) lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    QrScannerContent(
        permisoConcedido = permisoConcedido,
        mensaje = mensaje,
        onPedirPermiso = { lanzadorPermiso.launch(Manifest.permission.CAMERA) },
        onAbrirAjustes = {
            // Para cuando el permiso fue negado "para siempre"
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
                            mensaje = "¡+${resultado.punto.puntosRecompensa} puntos en ${resultado.punto.titulo}!"
                            onVerVideo(resultado.punto.id)
                        }
                        is ResultadoEscaneo.YaVisitado ->
                            mensaje = "Ya visitaste ${resultado.punto.titulo}"
                        ResultadoEscaneo.Invalido ->
                            mensaje = "Este código QR no pertenece a la app"
                    }
                    // Pausa de 2 segundos antes de aceptar otra lectura
                    alcance.launch {
                        delay(2000)
                        procesando = false
                    }
                }
            }
        }
    )
}

/** Versión "stateless": solo dibuja según lo que recibe. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerContent(
    permisoConcedido: Boolean,
    mensaje: String,
    onPedirPermiso: () -> Unit,
    onAbrirAjustes: () -> Unit,
    onCodigoDetectado: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Escáner QR") }) }
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
                // Mensaje flotante sobre la cámara
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Sin permiso: explica y ofrece las dos salidas
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Necesitamos acceso a la cámara para leer los códigos QR",
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onPedirPermiso) { Text("Dar permiso") }
                    OutlinedButton(onClick = onAbrirAjustes) { Text("Abrir ajustes de la app") }
                }
            }
        }
    }
}

/**
 * Vista previa de la cámara + lectura de QR con ML Kit.
 * CameraX se encarga de la cámara; ML Kit analiza cada fotograma.
 */
@Composable
private fun CamaraQr(
    onCodigoDetectado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current
    // Siempre usa la versión más reciente del callback
    val alDetectar by rememberUpdatedState(onCodigoDetectado)

    val controlador = remember { LifecycleCameraController(contexto) }

    DisposableEffect(ciclo) {
        // Escáner configurado solo para códigos QR (más rápido)
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
        // La cámara se enciende y apaga sola según el ciclo de vida de la pantalla
        controlador.bindToLifecycle(ciclo)

        onDispose {
            controlador.clearImageAnalysisAnalyzer()
            controlador.unbind()
            escaner.close()
        }
    }

    // PreviewView es una vista clásica de Android; AndroidView la mete en Compose
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply { this.controller = controlador }
        }
    )
}