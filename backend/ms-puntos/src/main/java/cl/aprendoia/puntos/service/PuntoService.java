package cl.aprendoia.puntos.service;

import cl.aprendoia.puntos.exception.ConflictoException;
import cl.aprendoia.puntos.exception.RecursoNoEncontradoException;
import cl.aprendoia.puntos.model.Punto;
import cl.aprendoia.puntos.repository.PuntoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Lógica de negocio. El controller solo recibe/devuelve HTTP; las reglas viven aquí. */
@Service
public class PuntoService {

    private final PuntoRepository repositorio;

    public PuntoService(PuntoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Punto> listar() {
        return repositorio.findAll();
    }

    public Punto obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el punto con id " + id));
    }

    public Punto obtenerPorQr(String codigoQR) {
        return repositorio.findByCodigoQR(codigoQR)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un punto con el QR " + codigoQR));
    }

    public Punto crear(Punto punto) {
        if (repositorio.existsByCodigoQR(punto.getCodigoQR())) {
            throw new ConflictoException("Ya existe un punto con el QR " + punto.getCodigoQR());
        }
        punto.setId(null); // el id lo genera la base de datos
        return repositorio.save(punto);
    }

    public Punto actualizar(Long id, Punto datos) {
        Punto actual = obtener(id);

        boolean cambiaQr = !actual.getCodigoQR().equals(datos.getCodigoQR());
        if (cambiaQr && repositorio.existsByCodigoQR(datos.getCodigoQR())) {
            throw new ConflictoException("Ya existe un punto con el QR " + datos.getCodigoQR());
        }

        actual.setTitulo(datos.getTitulo());
        actual.setConceptoIA(datos.getConceptoIA());
        actual.setLatitud(datos.getLatitud());
        actual.setLongitud(datos.getLongitud());
        actual.setCodigoQR(datos.getCodigoQR());
        actual.setPuntosRecompensa(datos.getPuntosRecompensa());
        actual.setUrlVideo(datos.getUrlVideo());
        return repositorio.save(actual);
    }

    public void eliminar(Long id) {
        repositorio.delete(obtener(id));
    }
}
