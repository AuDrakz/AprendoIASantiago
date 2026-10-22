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

    @Test
    fun telefonoValido_esAceptado() {
        assertTrue(Validaciones.telefonoValido("+56 9 1234 5678"))
        assertTrue(Validaciones.telefonoValido("912345678"))
    }

    @Test
    fun telefonoInvalido_esRechazado() {
        assertFalse(Validaciones.telefonoValido(""))
        assertFalse(Validaciones.telefonoValido("1234"))
        assertFalse(Validaciones.telefonoValido("abc12345678"))
    }

    @Test
    fun contrasena_exigeMinimoDeCaracteres() {
        assertTrue(Validaciones.contrasenaValida("abcd"))
        assertFalse(Validaciones.contrasenaValida("123"))
    }
}