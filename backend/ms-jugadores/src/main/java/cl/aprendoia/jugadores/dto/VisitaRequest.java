package cl.aprendoia.jugadores.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de POST /api/jugadores/{id}/visitas */
public record VisitaRequest(
        @NotNull(message = "El id del punto es obligatorio") Long puntoId,
        @NotNull(message = "Los puntos son obligatorios") @Min(value = 0, message = "Los puntos no pueden ser negativos") Integer puntos
) {
}
