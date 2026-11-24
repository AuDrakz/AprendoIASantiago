package cl.aprendoia.puntos.service;

import cl.aprendoia.puntos.exception.ConflictoException;
import cl.aprendoia.puntos.exception.RecursoNoEncontradoException;
import cl.aprendoia.puntos.model.Punto;
import cl.aprendoia.puntos.repository.PuntoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El repositorio se "mockea": el test prueba solo la lógica del servicio, sin base de datos. */
@ExtendWith(MockitoExtension.class)
class PuntoServiceTest {

    @Mock
    private PuntoRepository repositorio;

    @InjectMocks
    private PuntoService servicio;

    private Punto punto(String qr) {
        return new Punto("Plaza de Armas", "NLP", -33.43, -70.65, qr, 10, "https://aprendoia.cl/v.mp4");
    }

    @Test
    void listar_devuelveTodosLosPuntosDelRepositorio() {
        when(repositorio.findAll()).thenReturn(List.of(punto("QR_A"), punto("QR_B")));

        assertEquals(2, servicio.listar().size());
    }

    @Test
    void obtener_devuelveElPuntoSiExiste() {
        Punto esperado = punto("QR_1");
        when(repositorio.findById(1L)).thenReturn(Optional.of(esperado));

        assertEquals("QR_1", servicio.obtener(1L).getCodigoQR());
    }

    @Test
    void obtener_lanzaExcepcionSiNoExiste() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(99L));
    }

    @Test
    void obtenerPorQr_devuelveElPuntoSiExiste() {
        when(repositorio.findByCodigoQR("QR_1")).thenReturn(Optional.of(punto("QR_1")));

        assertEquals("QR_1", servicio.obtenerPorQr("QR_1").getCodigoQR());
    }

    @Test
    void obtenerPorQr_lanzaExcepcionSiNoExiste() {
        when(repositorio.findByCodigoQR("QR_NO")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtenerPorQr("QR_NO"));
    }

    @Test
    void crear_guardaElPuntoSiElQrEsNuevo() {
        Punto nuevo = punto("QR_NUEVO");
        when(repositorio.existsByCodigoQR("QR_NUEVO")).thenReturn(false);
        when(repositorio.save(nuevo)).thenReturn(nuevo);

        assertEquals("QR_NUEVO", servicio.crear(nuevo).getCodigoQR());
        verify(repositorio).save(nuevo);
    }

    @Test
    void crear_rechazaUnQrRepetido() {
        when(repositorio.existsByCodigoQR("QR_REPETIDO")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> servicio.crear(punto("QR_REPETIDO")));
        verify(repositorio, never()).save(any());
    }

    @Test
    void actualizar_modificaLosCamposYGuardaSinConsultarElQrSiNoCambia() {
        Punto existente = punto("QR_A");
        Punto datos = new Punto("Nuevo titulo", "NLP 2", -33.0, -70.0, "QR_A", 25, "https://aprendoia.cl/n.mp4");
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));
        when(repositorio.save(existente)).thenReturn(existente);

        Punto resultado = servicio.actualizar(1L, datos);

        assertEquals("Nuevo titulo", resultado.getTitulo());
        assertEquals(25, resultado.getPuntosRecompensa());
        verify(repositorio).save(existente);
        verify(repositorio, never()).existsByCodigoQR(any());
    }

    @Test
    void actualizar_rechazaCambiarAUnQrQueYaUsaOtroPunto() {
        Punto existente = punto("QR_A");
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));
        when(repositorio.existsByCodigoQR("QR_B")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> servicio.actualizar(1L, punto("QR_B")));
    }

    @Test
    void eliminar_borraElPuntoExistente() {
        Punto existente = punto("QR_A");
        when(repositorio.findById(1L)).thenReturn(Optional.of(existente));

        servicio.eliminar(1L);

        verify(repositorio).delete(existente);
    }

    @Test
    void eliminar_lanzaExcepcionSiNoExisteYNoBorraNada() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.eliminar(99L));
        verify(repositorio, never()).delete(any());
    }
    @Test
    void actualizar_noValidaQrRepetidoSiElQrNoCambia() {
        Punto actual = punto("QR_1");
        when(repositorio.findById(1L)).thenReturn(Optional.of(actual));
        when(repositorio.save(any(Punto.class))).thenAnswer(inv -> inv.getArgument(0));

        Punto cambios = new Punto("Nuevo título", "NLP", -33.43, -70.65, "QR_1", 20, "https://aprendoia.cl/v.mp4");
        Punto resultado = servicio.actualizar(1L, cambios);

        assertEquals("Nuevo título", resultado.getTitulo());
        verify(repositorio, never()).existsByCodigoQR(any());
    }
}