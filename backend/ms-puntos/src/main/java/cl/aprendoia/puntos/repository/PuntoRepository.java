package cl.aprendoia.puntos.repository;

import cl.aprendoia.puntos.model.Punto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data genera la implementación a partir de los nombres de los métodos. */
public interface PuntoRepository extends JpaRepository<Punto, Long> {

    Optional<Punto> findByCodigoQR(String codigoQR);

    boolean existsByCodigoQR(String codigoQR);
}
