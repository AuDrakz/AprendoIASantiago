package com.aprendoiasantiago.util

/** Reglas de validación puras (sin Android): se pueden probar en tests JVM. */
object Validaciones {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val TELEFONO = Regex("^\\+?[0-9]{8,12}$")

    const val MIN_CONTRASENA = 4

    fun emailValido(valor: String): Boolean = EMAIL.matches(valor.trim())

    /** Acepta espacios (+56 9 1234 5678): 8 a 12 dígitos, con "+" opcional. */
    fun telefonoValido(valor: String): Boolean =
        TELEFONO.matches(valor.filterNot { it.isWhitespace() })

    fun contrasenaValida(valor: String): Boolean = valor.length >= MIN_CONTRASENA
}