package cl.aprendoia.jugadores.repository;

import cl.aprendoia.jugadores.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByEmail(String email);

    List<Jugador> findAllByOrderByPuntosDesc();
}
