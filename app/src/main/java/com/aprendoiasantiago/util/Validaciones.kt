package com.aprendoiasantiago.util

/** Reglas de validación puras (sin Android): se pueden probar en tests JVM. */
object Validaciones {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun emailValido(valor: String): Boolean = EMAIL.matches(valor.trim())
}