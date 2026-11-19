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
}
