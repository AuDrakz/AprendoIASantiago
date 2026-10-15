package com.aprendoiasantiago.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesTest {

    @Test
    fun correoValido_esAceptado() {
        assertTrue(Validaciones.emailValido("estudiante@duoc.cl"))
        assertTrue(Validaciones.emailValido("  ana.perez+ia@correo.com  "))
    }

    @Test
    fun correoInvalido_esRechazado() {
        assertFalse(Validaciones.emailValido(""))
        assertFalse(Validaciones.emailValido("sin-arroba.cl"))
        assertFalse(Validaciones.emailValido("a@b"))
        assertFalse(Validaciones.emailValido("@duoc.cl"))
    }
}