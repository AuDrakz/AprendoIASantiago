package cl.aprendoia.puntos.controller;

import cl.aprendoia.puntos.model.Punto;
import cl.aprendoia.puntos.service.PuntoService;
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
@RequestMapping("/api/puntos")
public class PuntoController {

    private final PuntoService servicio;

    public PuntoController(PuntoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Punto> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public Punto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @GetMapping("/qr/{codigoQR}")
    public Punto obtenerPorQr(@PathVariable String codigoQR) {
        return servicio.obtenerPorQr(codigoQR);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Punto crear(@Valid @RequestBody Punto punto) {
        return servicio.crear(punto);
    }

    @PutMapping("/{id}")
    public Punto actualizar(@PathVariable Long id, @Valid @RequestBody Punto punto) {
        return servicio.actualizar(id, punto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
