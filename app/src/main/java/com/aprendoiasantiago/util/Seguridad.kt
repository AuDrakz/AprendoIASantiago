package com.aprendoiasantiago.util

import java.security.MessageDigest
import java.security.SecureRandom

/** Contraseñas con sal + SHA-256. Pura JVM: se puede probar en tests. */
object Seguridad {

    private val aleatorio = SecureRandom()

    /** Devuelve "sal:hash". Nunca se guarda la contraseña en claro. */
    fun hashear(contrasena: String): String {
        val sal = ByteArray(16).also { aleatorio.nextBytes(it) }.aHex()
        return "$sal:${resumen(sal, contrasena)}"
    }

    fun verificar(contrasena: String, guardado: String): Boolean {
        val partes = guardado.split(":")
        if (partes.size != 2) return false
        return MessageDigest.isEqual(
            resumen(partes[0], contrasena).toByteArray(),
            partes[1].toByteArray()
        )
    }

    private fun resumen(sal: String, contrasena: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((sal + contrasena).toByteArray())
            .aHex()

    private fun ByteArray.aHex(): String = joinToString("") { "%02x".format(it) }
}