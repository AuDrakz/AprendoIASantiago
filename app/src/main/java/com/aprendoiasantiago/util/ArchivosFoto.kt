package com.aprendoiasantiago.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Guarda fotos en filesDir/fotos (coincide con file_paths.xml). */
object ArchivosFoto {

    private fun carpeta(c: Context) = File(c.filesDir, "fotos").apply { mkdirs() }

    /** Nombre único: evita que Coil muestre una imagen vieja en caché. */
    fun nuevoArchivo(c: Context): File =
        File(carpeta(c), "foto_${System.currentTimeMillis()}.jpg")

    fun uriDe(c: Context, archivo: File): Uri =
        FileProvider.getUriForFile(c, "${c.packageName}.fileprovider", archivo)

    /** Copia una imagen de la galería a la app. Devuelve la ruta o null si falla. */
    fun copiar(c: Context, origen: Uri): String? = runCatching {
        val destino = nuevoArchivo(c)
        c.contentResolver.openInputStream(origen)?.use { entrada ->
            destino.outputStream().use { salida -> entrada.copyTo(salida) }
        }?.let { destino.absolutePath }
    }.getOrNull()
}