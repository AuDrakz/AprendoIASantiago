package com.aprendoiasantiago.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aprendoiasantiago.model.ResultadoEscaneo
import com.aprendoiasantiago.util.ArchivosFoto
import com.aprendoiasantiago.viewmodel.EscanerViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

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
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var permisoUbicacion by remember { mutableStateOf(tienePermisoUbicacionQr(context)) }
    var mensaje by remember { mutableStateOf("Apunta al QR y toma la foto.") }
    var resultadoDialogo by remember { mutableStateOf<String?>(null) }
    var resultadoValidacion by remember { mutableStateOf<ResultadoEscaneo?>(null) }
    var resultadoSelfie by remember { mutableStateOf<String?>(null) }
    var reiniciarEscaner by remember { mutableStateOf(0) }

    var usarFrontal by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var procesando by remember { mutableStateOf(false) }

    val permisosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permisos: Map<String, Boolean> ->
            permisoCamara =
                permisos[Manifest.permission.CAMERA] == true ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
            permisoUbicacion =
                permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                        tienePermisoUbicacionQr(context)
        }
    )

    LaunchedEffect(Unit) {
        val necesarios = mutableListOf<String>()
        if (!permisoCamara) necesarios.add(Manifest.permission.CAMERA)
        if (!permisoUbicacion) {
            necesarios.add(Manifest.permission.ACCESS_FINE_LOCATION)
            necesarios.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (necesarios.isNotEmpty()) permisosLauncher.launch(necesarios.toTypedArray())
    }

    fun tomarFoto() {
        val captura = imageCapture ?: return
        if (procesando) return
        procesando = true
        val archivo = ArchivosFoto.nuevoArchivo(context)

        captura.takePicture(
            ImageCapture.OutputFileOptions.Builder(archivo).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(resultados: ImageCapture.OutputFileResults) {
                    val ruta = ArchivosFoto.optimizar(archivo.absolutePath)
                    if (ruta == null) {
                        procesando = false
                        mensaje = "No se pudo guardar la foto."
                        return
                    }

                    val esSelfie = usarFrontal

                    // La validación del QR (y rostro) se hace DESPUÉS de tomar la foto.
                    analizarFoto(ruta, buscarRostro = esSelfie) { codigo, hayRostro ->
                        if (!esSelfie) {
                            ArchivosFoto.borrar(ruta)
                            procesando = false
                            if (codigo == null) {
                                mensaje = "No se detectó ningún QR. Intenta de nuevo."
                            } else {
                                mensaje = "Código QR detectado."
                                resultadoDialogo = codigo
                            }
                            return@analizarFoto
                        }

                        if (codigo == null || !hayRostro) {
                            ArchivosFoto.borrar(ruta)
                            procesando = false
                            resultadoSelfie = when {
                                codigo == null && !hayRostro -> "No se detectó tu rostro ni el QR."
                                codigo == null -> "No se detectó el QR en la foto."
                                else -> "No se detectó tu rostro en la foto."
                            }
                            return@analizarFoto
                        }

                        val ubicacion = obtenerUltimaUbicacionQr(context)
                        viewModel.registrarSelfie(
                            codigoLeido = codigo.trim(),
                            latitudUsuario = ubicacion?.latitud,
                            longitudUsuario = ubicacion?.longitud,
                            altitudUsuario = ubicacion?.altitud,
                            ruta = ruta
                        ) { resultado, bonus ->
                            procesando = false

                            if (bonus) ArchivosFoto.guardarEnGaleria(context, ruta)
                            else ArchivosFoto.borrar(ruta)

                            resultadoSelfie = when {
                                bonus ->
                                    "¡Selfie válida! +100 puntos de bonificación" +
                                            if (resultado is ResultadoEscaneo.Exito) " y +10 por el QR." else "."

                                resultado is ResultadoEscaneo.YaVisitado ->
                                    "Ya tenías la selfie de este lugar. No se suman puntos otra vez."

                                resultado is ResultadoEscaneo.FueraDeRango ->
                                    "Estás a ${resultado.distanciaMetros.toInt()} m del punto. Acércate para validar."

                                resultado is ResultadoEscaneo.Invalido ->
                                    "QR no reconocido: no existe en la aplicación."

                                resultado is ResultadoEscaneo.SinUbicacion ->
                                    "Activa el GPS y espera a obtener ubicación."

                                else -> "No se pudo validar la selfie."
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    procesando = false
                    mensaje = "No se pudo tomar la foto."
                }
            }
        )
    }

    if (permisoCamara && permisoUbicacion) {
        Box(modifier = Modifier.fillMaxSize()) {
            key(reiniciarEscaner, usarFrontal) {
                CamaraPreview(
                    modifier = Modifier.fillMaxSize(),
                    usarFrontal = usarFrontal,
                    onImageCapture = { imageCapture = it }
                )
            }

            // Mensaje superior con fondo para que siempre se lea
            Text(
                text = if (usarFrontal) "Selfie: muestra tu rostro y el QR" else mensaje,
                color = ComposeColor.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ComposeColor(0xCC212121))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )

            // Barra inferior: [vacío] [obturador] [girar cámara]
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.size(52.dp))

                // Botón de cámara convencional
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .border(4.dp, ComposeColor.White, CircleShape)
                        .padding(7.dp)
                        .clip(CircleShape)
                        .background(
                            if (procesando) ComposeColor.Gray else ComposeColor.White
                        )
                        .clickable(enabled = !procesando && imageCapture != null) {
                            tomarFoto()
                        }
                )

                // Girar cámara (flechas)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ComposeColor(0xCC212121))
                        .clickable(enabled = !procesando) {
                            usarFrontal = !usarFrontal
                            imageCapture = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Girar cámara",
                        tint = ComposeColor.White
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Se necesitan permisos de cámara y ubicación.")
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
            ) { Text("Solicitar permisos") }
        }
    }

    resultadoSelfie?.let { texto ->
        AlertDialog(
            onDismissRequest = { resultadoSelfie = null },
            title = { Text("Resultado de la selfie") },
            text = { Text(texto) },
            confirmButton = {
                TextButton(
                    onClick = {
                        resultadoSelfie = null
                        reiniciarEscaner++
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
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
                        "Altitud: ${resultado.punto.altitudVisita?.let { "$it m" } ?: "no disponible"}\n\n" +
                        "Si es un hito turístico, usa la cámara frontal y gana +100 puntos."

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
private fun CamaraPreview(
    modifier: Modifier = Modifier,
    usarFrontal: Boolean,
    onImageCapture: (ImageCapture?) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            val previewView = PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val providerFuture = ProcessCameraProvider.getInstance(viewContext)

            providerFuture.addListener(
                {
                    try {
                        val provider = providerFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val captura = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        val selector = if (usarFrontal) CameraSelector.DEFAULT_FRONT_CAMERA
                        else CameraSelector.DEFAULT_BACK_CAMERA

                        provider.unbindAll()
                        provider.bindToLifecycle(lifecycleOwner, selector, preview, captura)
                        onImageCapture(captura)
                    } catch (exception: Exception) {
                        onImageCapture(null)
                    }
                },
                ContextCompat.getMainExecutor(viewContext)
            )
            previewView
        }
    )
}

/** Analiza la foto ya tomada: busca QR y (opcional) un rostro. */
private fun analizarFoto(
    ruta: String,
    buscarRostro: Boolean,
    onListo: (codigo: String?, hayRostro: Boolean) -> Unit
) {
    val bitmap = BitmapFactory.decodeFile(ruta)
    if (bitmap == null) {
        onListo(null, false)
        return
    }
    val imagen = InputImage.fromBitmap(bitmap, 0)

    val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )
    val detector = if (buscarRostro) {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
        )
    } else null

    val tareaQr = scanner.process(imagen)
    val tareaRostro = detector?.process(imagen)

    Tasks.whenAllComplete(listOfNotNull<Task<*>>(tareaQr, tareaRostro))
        .addOnCompleteListener {
            val codigo = if (tareaQr.isSuccessful) {
                tareaQr.result
                    .firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                    ?.rawValue
                    ?.takeIf { it.isNotBlank() }
            } else null

            val hayRostro = tareaRostro != null &&
                    tareaRostro.isSuccessful &&
                    tareaRostro.result.isNotEmpty()

            scanner.close()
            detector?.close()
            onListo(codigo, hayRostro)
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