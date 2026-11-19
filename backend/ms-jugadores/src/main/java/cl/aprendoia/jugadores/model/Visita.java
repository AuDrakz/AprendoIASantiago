package cl.aprendoia.jugadores.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Un jugador visitó un punto. La restricción única (jugador, punto) impide
 * sumar puntos dos veces por el mismo lugar, igual que la regla actual de la app.
 * Guarda solo el id del punto: el catálogo vive en otro microservicio (ms-puntos).
 */
@Entity
@Table(name = "visitas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"jugadorId", "puntoId"}))
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jugadorId;

    private Long puntoId;

    private LocalDateTime fecha;

    public Visita() {
    }

    public Visita(Long jugadorId, Long puntoId, LocalDateTime fecha) {
        this.jugadorId = jugadorId;
        this.puntoId = puntoId;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJugadorId() { return jugadorId; }
    public void setJugadorId(Long jugadorId) { this.jugadorId = jugadorId; }

    public Long getPuntoId() { return puntoId; }
    public void setPuntoId(Long puntoId) { this.puntoId = puntoId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
