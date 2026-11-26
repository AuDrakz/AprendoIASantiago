package com.aprendoiasantiago.data

import com.aprendoiasantiago.data.remote.BuscadorDirecciones
import com.aprendoiasantiago.data.remote.PhotonFeature
import com.aprendoiasantiago.data.remote.PhotonGeometry
import com.aprendoiasantiago.data.remote.PhotonProps
import com.aprendoiasantiago.data.remote.PhotonRespuesta
import com.aprendoiasantiago.data.remote.RetrofitService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/** API externa (Photon vía Retrofit): se reemplaza el servicio por un mock. */
class BuscadorDireccionesTest {

    private val original = BuscadorDirecciones.servicio
    private val servicio = mockk<RetrofitService>()

    @Before
    fun preparar() {
        BuscadorDirecciones.servicio = servicio
    }

    @After
    fun restaurar() {
        BuscadorDirecciones.servicio = original
    }

    private fun respuesta(vararg f: PhotonFeature) = PhotonRespuesta(features = f.toList())

    private fun feature(
        props: PhotonProps,
        coordenadas: List<Double> = listOf(-70.65, -33.43) // Photon entrega [lon, lat]
    ) = PhotonFeature(props, PhotonGeometry(coordenadas))

    private fun dado(r: PhotonRespuesta) {
        coEvery { servicio.buscar(any(), any(), any(), any()) } returns r
    }

    @Test
    fun consultaCorta_devuelveListaVaciaSinLlamarALaApi() = runTest {
        assertEquals(emptyList<Any>(), BuscadorDirecciones.buscar("  ab "))

        coVerify(exactly = 0) { servicio.buscar(any(), any(), any(), any()) }
    }

    @Test
    fun lugarEnChile_seConvierteEnSugerenciaConZona() = runTest {
        dado(respuesta(feature(PhotonProps(
            name = "Plaza de Armas", city = "Santiago", state = "Región Metropolitana", country = "Chile"
        ))))

        val r = BuscadorDirecciones.buscar("plaza de armas")

        assertEquals(1, r.size)
        assertEquals("Plaza de Armas", r[0].nombre)
        assertEquals("Santiago, Región Metropolitana", r[0].direccion)
        assertEquals(-33.43, r[0].latitud, 0.0001)
        assertEquals(-70.65, r[0].longitud, 0.0001)
        coVerify { servicio.buscar("plaza de armas", any(), any(), any()) }
    }

    @Test
    fun sinNombre_usaLaCalleYElNumeroComoNombre() = runTest {
        dado(respuesta(feature(PhotonProps(
            street = "Calle Larga", housenumber = "12", city = "Santiago", country = "Chile"
        ))))

        val r = BuscadorDirecciones.buscar("calle larga")

        assertEquals("Calle Larga 12", r.single().nombre)
        assertEquals("Santiago", r.single().direccion)
    }

    @Test
    fun descartaResultadosFueraDeChileSinCoordenadasOSinNombre() = runTest {
        dado(respuesta(
            feature(PhotonProps(name = "Plaza", country = "Argentina")),
            feature(PhotonProps(name = "Sin coordenadas", country = "Chile"), coordenadas = emptyList()),
            feature(PhotonProps(name = "Una sola", country = "Chile"), coordenadas = listOf(-70.6)),
            feature(PhotonProps(name = " ", country = "Chile")),
            feature(PhotonProps(name = "Válido", country = "Chile"))
        ))

        val r = BuscadorDirecciones.buscar("plaza")

        assertEquals(listOf("Válido"), r.map { it.nombre })
    }

    @Test
    fun siLaApiFalla_devuelveListaVacia() = runTest {
        coEvery { servicio.buscar(any(), any(), any(), any()) } throws IOException("sin red")

        assertTrue(BuscadorDirecciones.buscar("plaza").isEmpty())
    }

    @Test
    fun siSeCancela_laRelanzaEnVezDeTragarsela() = runTest {
        coEvery { servicio.buscar(any(), any(), any(), any()) } throws CancellationException("cancelada")

        val error = runCatching { BuscadorDirecciones.buscar("plaza") }.exceptionOrNull()

        assertTrue(error is CancellationException)
    }
}
