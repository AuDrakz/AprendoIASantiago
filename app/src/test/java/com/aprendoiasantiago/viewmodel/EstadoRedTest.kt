package com.aprendoiasantiago.viewmodel

import com.aprendoiasantiago.httpError
import com.aprendoiasantiago.util.ErrorRed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException

class EstadoRedTest {

    @Test
    fun ejecutar_exito_devuelveElValorYApagaLaCarga() = runTest {
        val red = EstadoRed()
        var cargandoDentro = false

        val r = red.ejecutar("ok") {
            cargandoDentro = red.cargando.value
            42
        }

        assertEquals(42, r)
        assertTrue(cargandoDentro)
        assertFalse(red.cargando.value)
        assertNull(red.error.value)
    }

    @Test
    fun ejecutar_conFallo_devuelveNuloPublicaElErrorYApagaLaCarga() = runTest {
        val red = EstadoRed()

        val r = red.ejecutar<Int>("falla") { throw httpError(409) }

        assertNull(r)
        assertEquals(ErrorRed.CONFLICTO, red.error.value)
        assertFalse(red.cargando.value)
    }

    @Test
    fun ejecutar_sinMostrarCarga_noTocaElIndicador() = runTest {
        val red = EstadoRed()
        var cargandoDentro = true

        red.ejecutar("silencioso", mostrarCarga = false) { cargandoDentro = red.cargando.value }

        assertFalse(cargandoDentro)
    }

    @Test
    fun ejecutar_seRelanzaLaCancelacionSinPublicarError() = runTest {
        val red = EstadoRed()

        val resultado = runCatching { red.ejecutar<Int>("cancel") { throw CancellationException("x") } }

        assertTrue(resultado.exceptionOrNull() is CancellationException)
        assertNull(red.error.value)
        assertFalse(red.cargando.value)
    }

    @Test
    fun limpiarError_borraElErrorPublicado() = runTest {
        val red = EstadoRed()
        red.ejecutar<Int>("falla") { throw SocketTimeoutException() }
        assertEquals(ErrorRed.TIMEOUT, red.error.value)

        red.limpiarError()

        assertNull(red.error.value)
    }
}
