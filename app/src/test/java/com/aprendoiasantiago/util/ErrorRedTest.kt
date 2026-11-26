package com.aprendoiasantiago.util

import com.aprendoiasantiago.httpError
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class ErrorRedTest {

    @Test
    fun excepcionesDeRed_seTraducenASuErrorDeRed() {
        assertEquals(ErrorRed.TIMEOUT, SocketTimeoutException().aErrorRed())
        assertEquals(ErrorRed.SIN_CONEXION, IOException("sin red").aErrorRed())
    }

    @Test
    fun codigosHttp_seTraducenASuErrorDeRed() {
        assertEquals(ErrorRed.DATOS_INVALIDOS, httpError(400).aErrorRed())
        assertEquals(ErrorRed.NO_ENCONTRADO, httpError(404).aErrorRed())
        assertEquals(ErrorRed.CONFLICTO, httpError(409).aErrorRed())
        assertEquals(ErrorRed.SERVICIO_NO_DISPONIBLE, httpError(503).aErrorRed())
        assertEquals(ErrorRed.DESCONOCIDO, httpError(500).aErrorRed())
    }

    @Test
    fun otraExcepcion_esDesconocida() {
        assertEquals(ErrorRed.DESCONOCIDO, IllegalStateException("x").aErrorRed())
    }
}
