package cl.aprendoia.jugadores.service;

import cl.aprendoia.jugadores.client.PuntosClient;
import cl.aprendoia.jugadores.dto.EntradaRanking;
import cl.aprendoia.jugadores.dto.PuntoDto;
import cl.aprendoia.jugadores.dto.VisitaRequest;
import cl.aprendoia.jugadores.exception.ConflictoException;
import cl.aprendoia.jugadores.exception.RecursoNoEncontradoException;
import cl.aprendoia.jugadores.exception.ServicioNoDisponibleException;
import cl.aprendoia.jugadores.model.Jugador;
import cl.aprendoia.jugadores.model.Rol;
import cl.aprendoia.jugadores.model.Visita;
import cl.aprendoia.jugadores.repository.JugadorRepository;
import cl.aprendoia.jugadores.repository.VisitaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JugadorServiceTest {

    @Mock
    private JugadorRepository jugadores;

    @Mock
    private VisitaRepository visitas;

    @Mock
    private PuntosClient puntos;

    @InjectMocks
    private JugadorService servicio;

    private Jugador jugador(String nombre, int puntos) {
        return new Jugador(nombre, nombre + "@aprendoia.cl", "", Rol.CLIENTE, puntos);
    }

    // ---------- listar / obtener ----------

    @Test
    void listar_devuelveTodosLosJugadores() {
        when(jugadores.findAll()).thenReturn(List.of(jugador("ana", 0), jugador("luis", 0)));

        assertEquals(2, servicio.listar().size());
    }

    @Test
    void obtener_lanzaExcepcionSiNoExiste() {
        when(jugadores.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(5L));
    }

    // ---------- crear ----------

    @Test
    void crear_empiezaConCeroPuntosAunqueVenganOtros() {
        Jugador nuevo = jugador("ana", 999);
        when(jugadores.existsByNombreUsuario("ana")).thenReturn(false);
        when(jugadores.existsByEmail("ana@aprendoia.cl")).thenReturn(false);
        when(jugadores.save(nuevo)).thenReturn(nuevo);

        assertEquals(0, servicio.crear(nuevo).getPuntos());
    }

    @Test
    void crear_rechazaNombreDeUsuarioRepetido() {
        when(jugadores.existsByNombreUsuario("ana")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> servicio.crear(jugador("ana", 0)));
        verify(jugadores, never()).save(any());
    }

    @Test
    void crear_rechazaCorreoRepetido() {
        when(jugadores.existsByNombreUsuario("ana")).thenReturn(false);
        when(jugadores.existsByEmail("ana@aprendoia.cl")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> servicio.crear(jugador("ana", 0)));
        verify(jugadores, never()).save(any());
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_cambiaLosDatosPeroNoLosPuntos() {
        Jugador actual = jugador("ana", 20);
        Jugador datos = new Jugador("ana2", "ana2@aprendoia.cl", "555", Rol.GENERADOR, 999);
        when(jugadores.findById(1L)).thenReturn(Optional.of(actual));
        when(jugadores.existsByNombreUsuario("ana2")).thenReturn(false);
        when(jugadores.existsByEmail("ana2@aprendoia.cl")).thenReturn(false);
        when(jugadores.save(actual)).thenReturn(actual);

        Jugador resultado = servicio.actualizar(1L, datos);

        assertEquals("ana2", resultado.getNombreUsuario());
        assertEquals(Rol.GENERADOR, resultado.getRol());
        assertEquals(20, resultado.getPuntos());
    }

    @Test
    void actualizar_rechazaUnNombreDeUsuarioQueYaUsaOtro() {
        Jugador actual = jugador("ana", 0);
        Jugador datos = new Jugador("otro", "ana@aprendoia.cl", "", Rol.CLIENTE, 0);
        when(jugadores.findById(1L)).thenReturn(Optional.of(actual));
        when(jugadores.existsByNombreUsuario("otro")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> servicio.actualizar(1L, datos));
        verify(jugadores, never()).save(any());
    }

    @Test
    void actualizar_rechazaUnCorreoQueYaUsaOtro() {
        Jugador actual = jugador("ana", 0);
        Jugador datos = new Jugador("ana", "otro@aprendoia.cl", "", Rol.CLIENTE, 0);
        when(jugadores.findById(1L)).thenReturn(Optional.of(actual));
        when(jugadores.existsByEmail("otro@aprendoia.cl")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> servicio.actualizar(1L, datos));
        verify(jugadores, never()).save(any());
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_borraTambienLasVisitasDelJugador() {
        Jugador ana = jugador("ana", 0);
        when(jugadores.findById(1L)).thenReturn(Optional.of(ana));

        servicio.eliminar(1L);

        verify(visitas).deleteByJugadorId(1L);
        verify(jugadores).delete(ana);
    }

    // ---------- visitas ----------

    @Test
    void visitasDe_devuelveLasVisitasDelJugador() {
        when(jugadores.findById(1L)).thenReturn(Optional.of(jugador("ana", 0)));
        when(visitas.findByJugadorId(1L))
                .thenReturn(List.of(new Visita(1L, 7L, LocalDateTime.now())));

        assertEquals(1, servicio.visitasDe(1L).size());
    }

    @Test
    void visitasDe_lanzaExcepcionSiElJugadorNoExiste() {
        when(jugadores.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.visitasDe(5L));
        verify(visitas, never()).findByJugadorId(any());
    }

    @Test
    void registrarVisita_sumaLaRecompensaQueDevuelveMsPuntos() {
        Jugador ana = jugador("ana", 20);
        when(jugadores.findById(1L)).thenReturn(Optional.of(ana));
        when(visitas.existsByJugadorIdAndPuntoId(1L, 7L)).thenReturn(false);
        when(puntos.obtenerPunto(7L)).thenReturn(new PuntoDto(7L, 10));
        when(jugadores.save(ana)).thenReturn(ana);

        Jugador resultado = servicio.registrarVisita(1L, new VisitaRequest(7L));

        assertEquals(30, resultado.getPuntos());
        verify(visitas).save(any());
    }

    @Test
    void registrarVisita_noSumaDosVecesElMismoPunto() {
        Jugador ana = jugador("ana", 20);
        when(jugadores.findById(1L)).thenReturn(Optional.of(ana));
        when(visitas.existsByJugadorIdAndPuntoId(1L, 7L)).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> servicio.registrarVisita(1L, new VisitaRequest(7L)));
        assertEquals(20, ana.getPuntos());
        verify(puntos, never()).obtenerPunto(any());
        verify(visitas, never()).save(any());
    }

    @Test
    void registrarVisita_noGuardaNadaSiElPuntoNoExiste() {
        Jugador ana = jugador("ana", 20);
        when(jugadores.findById(1L)).thenReturn(Optional.of(ana));
        when(visitas.existsByJugadorIdAndPuntoId(1L, 99L)).thenReturn(false);
        when(puntos.obtenerPunto(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe el punto con id 99"));

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.registrarVisita(1L, new VisitaRequest(99L)));
        assertEquals(20, ana.getPuntos());
        verify(visitas, never()).save(any());
    }

    @Test
    void registrarVisita_propagaElErrorSiMsPuntosEstaCaido() {
        Jugador ana = jugador("ana", 20);
        when(jugadores.findById(1L)).thenReturn(Optional.of(ana));
        when(visitas.existsByJugadorIdAndPuntoId(1L, 7L)).thenReturn(false);
        when(puntos.obtenerPunto(7L)).thenThrow(new ServicioNoDisponibleException("caído"));

        assertThrows(ServicioNoDisponibleException.class,
                () -> servicio.registrarVisita(1L, new VisitaRequest(7L)));
        verify(visitas, never()).save(any());
    }

    // ---------- ranking ----------

    @Test
    void ranking_devuelveNombreYPuntosEnElOrdenDelRepositorio() {
        when(jugadores.findAllByOrderByPuntosDesc())
                .thenReturn(List.of(jugador("camila", 450), jugador("matias", 300)));

        List<EntradaRanking> ranking = servicio.ranking();

        assertEquals(2, ranking.size());
        assertEquals("camila", ranking.get(0).nombre());
        assertEquals(300, ranking.get(1).puntos());
    }
}