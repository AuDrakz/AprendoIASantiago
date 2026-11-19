package cl.aprendoia.jugadores.repository;

import cl.aprendoia.jugadores.model.Visita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    boolean existsByJugadorIdAndPuntoId(Long jugadorId, Long puntoId);

    List<Visita> findByJugadorId(Long jugadorId);

    void deleteByJugadorId(Long jugadorId);
}
