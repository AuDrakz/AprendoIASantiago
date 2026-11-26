package com.aprendoiasantiago.data

import com.aprendoiasantiago.data.local.PerfilEntity
import com.aprendoiasantiago.data.local.PuntoEntity
import com.aprendoiasantiago.data.local.RankingEntity
import com.aprendoiasantiago.data.local.toEntity
import com.aprendoiasantiago.data.local.toModel
import com.aprendoiasantiago.data.remote.JugadorDto
import com.aprendoiasantiago.data.remote.PuntoDto
import com.aprendoiasantiago.data.remote.aDto
import com.aprendoiasantiago.data.remote.aJugadorDto
import com.aprendoiasantiago.data.remote.aModelo
import com.aprendoiasantiago.data.remote.aPerfil
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.model.Rol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {

    private val entidad = PuntoEntity(
        id = 2, titulo = "Plaza", conceptoIA = "NLP", latitud = -33.43, longitud = -70.65,
        codigoQR = "QR_2", puntosRecompensa = 10, urlVideo = "u", fueVisitado = true,
        selfieRuta = "/s.jpg", fechaVisita = "2026-01-01", altitudVisita = 540.0
    )

    @Test
    fun punto_entidadAModeloYDeVuelta_conservaTodosLosCampos() {
        val modelo = entidad.toModel()

        assertEquals("Plaza", modelo.titulo)
        assertEquals(true, modelo.fueVisitado)
        assertEquals("/s.jpg", modelo.selfieRuta)
        assertEquals(entidad, modelo.toEntity())
    }

    @Test
    fun ranking_entidadAModelo() {
        val m = RankingEntity(nombre = "Luis", puntos = 30).toModel()

        assertEquals("Luis", m.nombre)
        assertEquals(30, m.puntos)
        assertFalse(m.esUsuarioActual)
    }

    @Test
    fun perfil_entidadAModelo_incluyePuntosYLugaresIndicados() {
        val e = PerfilEntity(
            id = 3, nombreUsuario = "Ana", alias = "A", email = "a@d.cl", telefono = "912345678",
            direccion = "Calle 1", avatar = 2, contrasenaHash = "x:y", rol = Rol.GENERADOR, fotoRuta = "/f.jpg"
        )

        val m = e.toModel(puntosTotales = 120, lugaresDescubiertos = 2)

        assertEquals(3, m.id)
        assertEquals(Rol.GENERADOR, m.rol)
        assertEquals("A", m.alias)
        assertEquals("/f.jpg", m.fotoRuta)
        assertEquals(120, m.puntosTotales)
        assertEquals(2, m.lugaresDescubiertos)
        assertEquals(0, e.toModel().puntosTotales)
    }

    @Test
    fun puntoDto_aModelo_usaCeroSiNoHayId() {
        val dto = PuntoDto(null, "T", "C", 1.0, 2.0, "QR", 5, "u")

        assertEquals(0, dto.aModelo().id)
        assertEquals(7, dto.copy(id = 7L).aModelo().id)
    }

    @Test
    fun puntoModelo_aDto_dejaElIdNuloCuandoEsNuevo() {
        val nuevo = PuntoInteres(0, "T", "C", 1.0, 2.0, "QR", 5, "u")

        assertNull(nuevo.aDto().id)
        assertEquals(4L, nuevo.copy(id = 4).aDto().id)
    }

    @Test
    fun jugadorDto_aPerfil_convierteRolYTelefonoNulo() {
        val dto = JugadorDto(id = 8, nombreUsuario = "Eva", email = "e@d.cl", telefono = null, rol = "ADMINISTRADOR", puntos = 60)

        val p = dto.aPerfil()

        assertEquals(8, p.id)
        assertEquals(Rol.ADMINISTRADOR, p.rol)
        assertEquals("", p.telefono)
        assertEquals(60, p.puntosTotales)
        assertEquals(0, dto.copy(id = null).aPerfil().id)
    }

    @Test
    fun perfilModelo_aJugadorDto_dejaElIdNuloCuandoEsNuevo() {
        val p = PerfilUsuario(id = 0, nombreUsuario = "Eva", email = "e@d.cl", rol = Rol.CLIENTE, telefono = "912345678", puntosTotales = 5)

        val dto = p.aJugadorDto()

        assertNull(dto.id)
        assertEquals("CLIENTE", dto.rol)
        assertEquals("912345678", dto.telefono)
        assertEquals(5, dto.puntos)
        assertEquals(9L, p.copy(id = 9).aJugadorDto().id)
    }
}
