package cl.aprendoia.jugadores.dto;

import jakarta.validation.constraints.NotNull;

public record VisitaRequest(
        @NotNull(message = "El id del punto es obligatorio") Long puntoId
) {
}