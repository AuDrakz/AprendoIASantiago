package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ErroresFormularioTest {

    @Test
    fun nombre_cubreTodosLosCasos() {
        assertEquals(R.string.error_nombre_obligatorio, ErroresFormulario.nombre("  "))
        assertEquals(R.string.error_nombre_min, ErroresFormulario.nombre(" ab "))
        assertEquals(R.string.error_nombre_max, ErroresFormulario.nombre("a".repeat(31)))
        assertNull(ErroresFormulario.nombre("Ana"))
        assertNull(ErroresFormulario.nombre("a".repeat(30)))
    }

    @Test
    fun email_cubreTodosLosCasos() {
        assertEquals(R.string.error_correo_obligatorio, ErroresFormulario.email(""))
        assertEquals(R.string.error_correo_invalido, ErroresFormulario.email("sin-arroba"))
        assertNull(ErroresFormulario.email("ana@duoc.cl"))
    }

    @Test
    fun telefono_cubreTodosLosCasos() {
        assertEquals(R.string.error_telefono_obligatorio, ErroresFormulario.telefono(" "))
        assertEquals(R.string.error_telefono_invalido, ErroresFormulario.telefono("123"))
        assertNull(ErroresFormulario.telefono("+56 9 1234 5678"))
    }

    @Test
    fun contrasena_cubreTodosLosCasos() {
        assertEquals(R.string.error_contrasena_obligatoria, ErroresFormulario.contrasena(""))
        assertEquals(R.string.error_contrasena_min, ErroresFormulario.contrasena("123"))
        assertNull(ErroresFormulario.contrasena("1234"))
    }
}
