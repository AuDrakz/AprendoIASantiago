package cl.aprendoia.jugadores.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** Convierte las excepciones del servicio en respuestas HTTP con un JSON uniforme. */
@RestControllerAdvice
public class ManejadorErrores {

    public record ErrorApi(int estado, String mensaje, List<String> detalles) {
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorApi> noEncontrado(RecursoNoEncontradoException e) {
        return construir(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorApi> conflicto(ConflictoException e) {
        return construir(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> validacion(MethodArgumentNotValidException e) {
        List<String> detalles = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Datos inválidos", detalles);
    }

    private ResponseEntity<ErrorApi> construir(HttpStatus estado, String mensaje, List<String> detalles) {
        return ResponseEntity.status(estado).body(new ErrorApi(estado.value(), mensaje, detalles));
    }
}
