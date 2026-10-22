package com.aprendoiasantiago.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aprendoiasantiago.model.ResultadoEscaneo
import com.aprendoiasantiago.viewmodel.EscanerViewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private data class UbicacionLeida(
    val latitud: Double,
    val longitud: Double,
    val altitud: Double?
)

@Composable
fun QrScannerScreen(
    viewModel: EscanerViewModel,
    onVerVideo: (Int) -> Unit
) {
    val context = LocalContext.current

    var permisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var permisoUbicacion by remember {
        mutableStateOf(tienePermisoUbicacionQr(context))
    }

    var mensaje by remember {
        mutableStateOf("Apunta la cámara al código QR.")
    }

    var resultadoDialogo by remember {
        mutableStateOf<String?>(null)
    }

    var resultadoValidacion by remember {
        mutableStateOf<ResultadoEscaneo?>(null)
    }

    var reiniciarEscaner by remember {
        mutableStateOf(0)
    }

    val permisosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permisos: Map<String, Boolean> ->
            permisoCamara =
                permisos[Manifest.permission.CAMERA] == true ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

            permisoUbicacion =
                permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                        tienePermisoUbicacionQr(context)

            mensaje = when {
                !permisoCamara ->
                    "Debes permitir el uso de la cámara para escanear QR."

                !permisoUbicacion ->
                    "Debes permitir la ubicación para validar la visita."

                else -> "Apunta la cámara al código QR."
            }
        }
    )

    LaunchedEffect(Unit) {
        val permisosNecesarios = mutableListOf<String>()

        if (!permisoCamara) {
            permisosNecesarios.add(Manifest.permission.CAMERA)
        }

        if (!permisoUbicacion) {
            permisosNecesarios.add(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            permisosNecesarios.add(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        if (permisosNecesarios.isNotEmpty()) {
            permisosLauncher.launch(permisosNecesarios.toTypedArray())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Escáner QR",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = mensaje,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        )

        if (permisoCamara && permisoUbicacion) {
            key(reiniciarEscaner) {
                QrCameraPreview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    onQrDetectado = { codigo ->
                        mensaje = "Código QR detectado."
                        resultadoDialogo = codigo
                    }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Se necesitan permisos de cámara y ubicación.")
            }

            Button(
                onClick = {
                    permisosLauncher.launch(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Solicitar permisos")
            }
        }
    }

    resultadoDialogo?.let { codigo ->
        AlertDialog(
            onDismissRequest = {
                resultadoDialogo = null
                mensaje = "Apunta la cámara al código QR."
            },
            title = {
                Text("Código QR detectado")
            },
            text = {
                Text(
                    "Contenido leído:\n$codigo\n\n" +
                            "Pulsa Aceptar para comprobar el código, " +
                            "la ubicación GPS y el estado de la visita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resultadoDialogo = null

                        val ubicacion = obtenerUltimaUbicacionQr(context)

                        viewModel.escanearCodigoQR(
                            codigoLeido = codigo.trim(),
                            latitudUsuario = ubicacion?.latitud,
                            longitudUsuario = ubicacion?.longitud,
                            onResultado = { resultado: ResultadoEscaneo ->
                                resultadoValidacion = resultado

                                mensaje = when (resultado) {
                                    is ResultadoEscaneo.Exito ->
                                        "Visita validada: ${resultado.punto.titulo}. " +
                                                "+${resultado.punto.puntosRecompensa} puntos."

                                    is ResultadoEscaneo.YaVisitado ->
                                        "Este punto ya estaba visitado. " +
                                                "No se sumaron puntos nuevamente."

                                    is ResultadoEscaneo.FueraDeRango ->
                                        "QR correcto, pero estás a " +
                                                "${resultado.distanciaMetros.toInt()} m. " +
                                                "Acércate al punto para validar."

                                    ResultadoEscaneo.Invalido ->
                                        "QR no reconocido: no existe en los puntos " +
                                                "de la aplicación."

                                    ResultadoEscaneo.SinUbicacion ->
                                        "No se pudo validar: activa el GPS y espera " +
                                                "a obtener ubicación."

                                    else ->
                                        "No se pudo completar la validación."
                                }
                            },
                            altitudUsuario = ubicacion?.altitud
                        )
                    }
                ) {
                    Text("Validar visita")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        resultadoDialogo = null
                        mensaje = "Apunta la cámara al siguiente código QR."
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    resultadoValidacion?.let { resultado ->
        val textoResultado = when (resultado) {
            is ResultadoEscaneo.Exito ->
                "VISITA VALIDADA\n" +
                        "Lugar: ${resultado.punto.titulo}\n" +
                        "Puntos obtenidos: ${resultado.punto.puntosRecompensa}\n" +
                        "Fecha: ${resultado.punto.fechaVisita ?: "guardada"}\n" +
                        "Altitud: ${resultado.punto.altitudVisita?.let { "$it m" } ?: "no disponible"}"

            is ResultadoEscaneo.YaVisitado ->
                "Este lugar ya estaba registrado como visitado. " +
                        "No se duplicaron los puntos."

            is ResultadoEscaneo.FueraDeRango ->
                "El código pertenece a ${resultado.punto.titulo}, " +
                        "pero estás a ${resultado.distanciaMetros.toInt()} metros. " +
                        "Debes acercarte para recibir los puntos."

            ResultadoEscaneo.Invalido ->
                "El código QR no existe en la base de datos de la aplicación."

            ResultadoEscaneo.SinUbicacion ->
                "No hay ubicación GPS disponible. " +
                        "Activa la ubicación y vuelve a intentarlo."

            else ->
                "No se pudo completar la validación de la visita."
        }

        AlertDialog(
            onDismissRequest = {
                resultadoValidacion = null
            },
            title = {
                Text(
                    if (resultado is ResultadoEscaneo.Exito) {
                        "Visita validada"
                    } else {
                        "Resultado de validación"
                    }
                )
            },
            text = {
                Text(textoResultado)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resultadoValidacion = null
                        reiniciarEscaner++
                        mensaje = "Apunta la cámara al siguiente código QR."
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun QrCameraPreview(
    modifier: Modifier = Modifier,
    onQrDetectado: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        BarcodeScanning.getClient(options)
    }

    val qrProcesado = remember {
        AtomicBoolean(false)
    }

    DisposableEffect(cameraExecutor, scanner) {
        onDispose {
            cameraExecutor.shutdown()
            scanner.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            val previewView = PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(viewContext)

            cameraProviderFuture.addListener(
                {
                    try {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(
                                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                            )
                            .build()

                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            analizarImagenQr(
                                imageProxy = imageProxy,
                                scanner = scanner,
                                qrProcesado = qrProcesado,
                                viewContext = viewContext,
                                onQrDetectado = onQrDetectado
                            )
                        }

                        cameraProvider.unbindAll()

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis
                        )
                    } catch (exception: Exception) {
                        qrProcesado.set(false)
                    }
                },
                ContextCompat.getMainExecutor(viewContext)
            )

            previewView
        }
    )
}

@OptIn(markerClass = [ExperimentalGetImage::class])
private fun analizarImagenQr(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    qrProcesado: AtomicBoolean,
    viewContext: Context,
    onQrDetectado: (String) -> Unit
) {
    val mediaImage = imageProxy.image

    if (mediaImage == null || qrProcesado.get()) {
        imageProxy.close()
        return
    }

    val image = try {
        InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
    } catch (exception: Exception) {
        imageProxy.close()
        return
    }

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val codigo = barcodes
                .firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                ?.rawValue

            if (
                !codigo.isNullOrBlank() &&
                qrProcesado.compareAndSet(false, true)
            ) {
                ContextCompat.getMainExecutor(viewContext).execute {
                    onQrDetectado(codigo)
                }
            }
        }
        .addOnFailureListener {
            // Si falla un fotograma, se continúa con los siguientes.
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

private fun tienePermisoUbicacionQr(context: Context): Boolean {
    val permisoPreciso = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val permisoAproximado = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return permisoPreciso || permisoAproximado
}

private fun obtenerUltimaUbicacionQr(
    context: Context
): UbicacionLeida? {
    if (!tienePermisoUbicacionQr(context)) {
        return null
    }

    return try {
        val locationManager = context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

        val proveedores = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )

        val ubicaciones = proveedores.mapNotNull { proveedor ->
            try {
                locationManager.getLastKnownLocation(proveedor)
            } catch (exception: SecurityException) {
                null
            }
        }

        val ubicacion: Location =
            ubicaciones.maxByOrNull { it.time } ?: return null

        UbicacionLeida(
            latitud = ubicacion.latitude,
            longitud = ubicacion.longitude,
            altitud = if (ubicacion.hasAltitude()) {
                ubicacion.altitude
            } else {
                null
            }
        )
    } catch (exception: Exception) {
        null
    }
}