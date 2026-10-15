package com.aprendoiasantiago.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Guarda fotos en filesDir/fotos (coincide con file_paths.xml). */
object ArchivosFoto {

    private const val LADO_MAXIMO = 1024
    private const val CALIDAD = 80

    private fun carpeta(c: Context) = File(c.filesDir, "fotos").apply { mkdirs() }

    /** Nombre único: evita que Coil muestre una imagen vieja en caché. */
    fun nuevoArchivo(c: Context): File =
        File(carpeta(c), "foto_${System.currentTimeMillis()}.jpg")

    fun uriDe(c: Context, archivo: File): Uri =
        FileProvider.getUriForFile(c, "${c.packageName}.fileprovider", archivo)

    fun borrar(ruta: String?) {
        ruta?.let { File(it).delete() }
    }

    /** Copia una imagen de la galería a la app y la optimiza. Devuelve la ruta o null. */
    fun copiar(c: Context, origen: Uri): String? {
        val destino = nuevoArchivo(c)
        val copiado = runCatching {
            c.contentResolver.openInputStream(origen)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } != null
        }.getOrDefault(false)
        return if (copiado) optimizar(destino.absolutePath) else descartar(destino.absolutePath)
    }

    /**
     * Reduce la resolución, corrige la rotación EXIF y recomprime en JPEG.
     * Devuelve la ruta, o null (y borra el archivo) si no es una imagen válida.
     */
    fun optimizar(ruta: String): String? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, limites)
        val lado = maxOf(limites.outWidth, limites.outHeight)
        if (lado <= 0) return descartar(ruta)

        var muestra = 1
        while (lado / (muestra * 2) >= LADO_MAXIMO) muestra *= 2
        val original = BitmapFactory.decodeFile(
            ruta, BitmapFactory.Options().apply { inSampleSize = muestra }
        ) ?: return descartar(ruta)

        val grados = runCatching {
            when (ExifInterface(ruta).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }.getOrDefault(0f)

        val imagen = if (grados == 0f) original else Bitmap.createBitmap(
            original, 0, 0, original.width, original.height,
            Matrix().apply { postRotate(grados) }, true
        )
        File(ruta).outputStream().use { imagen.compress(Bitmap.CompressFormat.JPEG, CALIDAD, it) }
        return ruta
    }

    private fun descartar(ruta: String): String? {
        borrar(ruta)
        return null
    }
}