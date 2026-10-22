package com.aprendoiasantiago.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeguridadTest {

    @Test
    fun contrasenaCorrecta_esVerificada() {
        val guardado = Seguridad.hashear("123")
        assertTrue(Seguridad.verificar("123", guardado))
    }

    @Test
    fun contrasenaIncorrecta_esRechazada() {
        val guardado = Seguridad.hashear("123")
        assertFalse(Seguridad.verificar("1234", guardado))
    }

    @Test
    fun mismaContrasena_generaHashesDistintosPorLaSal() {
        assertNotEquals(Seguridad.hashear("123"), Seguridad.hashear("123"))
    }

    @Test
    fun hashNoContieneLaContrasenaEnClaro() {
        assertFalse(Seguridad.hashear("clave-secreta").contains("clave-secreta"))
    }

    @Test
    fun hashMalFormado_esRechazado() {
        assertFalse(Seguridad.verificar("123", "sin-separador"))
    }
}