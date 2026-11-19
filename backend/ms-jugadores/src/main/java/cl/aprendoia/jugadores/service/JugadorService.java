package cl.aprendoia.jugadores.service;

import cl.aprendoia.jugadores.dto.EntradaRanking;
import cl.aprendoia.jugadores.dto.VisitaRequest;
import cl.aprendoia.jugadores.exception.ConflictoException;
import cl.aprendoia.jugadores.exception.RecursoNoEncontradoException;
import cl.aprendoia.jugadores.model.Jugador;
import cl.aprendoia.jugadores.model.Visita;
import cl.aprendoia.jugadores.repository.JugadorRepository;
import cl.aprendoia.jugadores.repository.VisitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JugadorService {

    private final JugadorRepository jugadores;
    private final VisitaRepository visitas;

    public JugadorService(JugadorRepository jugadores, VisitaRepository visitas) {
        this.jugadores = jugadores;
        this.visitas = visitas;
    }

    public List<Jugador> listar() {
        return jugadores.findAll();
    }

    public Jugador obtener(Long id) {
        return jugadores.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el jugador con id " + id));
    }

    public Jugador crear(Jugador jugador) {
        if (jugadores.existsByNombreUsuario(jugador.getNombreUsuario())) {
            throw new ConflictoException("El nombre de usuario ya está en uso");
        }
        if (jugadores.existsByEmail(jugador.getEmail())) {
            throw new ConflictoException("El correo ya está registrado");
        }
        jugador.setId(null);
        jugador.setPuntos(0); // los puntos se ganan solo visitando puntos
        return jugadores.save(jugador);
    }

    public Jugador actualizar(Long id, Jugador datos) {
        Jugador actual = obtener(id);

        if (!actual.getNombreUsuario().equals(datos.getNombreUsuario())
                && jugadores.existsByNombreUsuario(datos.getNombreUsuario())) {
            throw new ConflictoException("El nombre de usuario ya está en uso");
        }
        if (!actual.getEmail().equals(datos.getEmail()) && jugadores.existsByEmail(datos.getEmail())) {
            throw new ConflictoException("El correo ya está registrado");
        }

        actual.setNombreUsuario(datos.getNombreUsuario());
        actual.setEmail(datos.getEmail());
        actual.setTelefono(datos.getTelefono());
        actual.setRol(datos.getRol());
        // los puntos NO se editan por aquí
        return jugadores.save(actual);
    }

    @Transactional
    public void eliminar(Long id) {
        Jugador jugador = obtener(id);
        visitas.deleteByJugadorId(id);
        jugadores.delete(jugador);
    }

    /** Ranking global: jugadores ordenados de mayor a menor puntaje. */
    public List<EntradaRanking> ranking() {
        return jugadores.findAllByOrderByPuntosDesc().stream()
                .map(j -> new EntradaRanking(j.getNombreUsuario(), j.getPuntos()))
                .toList();
    }

    public List<Visita> visitasDe(Long jugadorId) {
        obtener(jugadorId); // 404 si el jugador no existe
        return visitas.findByJugadorId(jugadorId);
    }

    /**
     * Registra la visita y suma los puntos en una sola transacción:
     * o se guardan las dos cosas o ninguna. Si ya había visitado ese punto, responde 409.
     */
    @Transactional
    public Jugador registrarVisita(Long jugadorId, VisitaRequest solicitud) {
        Jugador jugador = obtener(jugadorId);

        if (visitas.existsByJugadorIdAndPuntoId(jugadorId, solicitud.puntoId())) {
            throw new ConflictoException("El jugador ya visitó este punto");
        }

        visitas.save(new Visita(jugadorId, solicitud.puntoId(), LocalDateTime.now()));
        jugador.setPuntos(jugador.getPuntos() + solicitud.puntos());
        return jugadores.save(jugador);
    }
}
