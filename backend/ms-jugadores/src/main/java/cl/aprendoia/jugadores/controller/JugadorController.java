package cl.aprendoia.jugadores.controller;

import cl.aprendoia.jugadores.dto.EntradaRanking;
import cl.aprendoia.jugadores.dto.VisitaRequest;
import cl.aprendoia.jugadores.model.Jugador;
import cl.aprendoia.jugadores.model.Visita;
import cl.aprendoia.jugadores.service.JugadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class JugadorController {

    private final JugadorService servicio;

    public JugadorController(JugadorService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/jugadores")
    public List<Jugador> listar() {
        return servicio.listar();
    }

    @GetMapping("/jugadores/{id}")
    public Jugador obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping("/jugadores")
    @ResponseStatus(HttpStatus.CREATED)
    public Jugador crear(@Valid @RequestBody Jugador jugador) {
        return servicio.crear(jugador);
    }

    @PutMapping("/jugadores/{id}")
    public Jugador actualizar(@PathVariable Long id, @Valid @RequestBody Jugador jugador) {
        return servicio.actualizar(id, jugador);
    }

    @DeleteMapping("/jugadores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }

    @GetMapping("/jugadores/{id}/visitas")
    public List<Visita> visitas(@PathVariable Long id) {
        return servicio.visitasDe(id);
    }

    @PostMapping("/jugadores/{id}/visitas")
    @ResponseStatus(HttpStatus.CREATED)
    public Jugador registrarVisita(@PathVariable Long id, @Valid @RequestBody VisitaRequest solicitud) {
        return servicio.registrarVisita(id, solicitud);
    }

    @GetMapping("/ranking")
    public List<EntradaRanking> ranking() {
        return servicio.ranking();
    }
}
