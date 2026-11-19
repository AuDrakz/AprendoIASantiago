package cl.aprendoia.jugadores.client;

import cl.aprendoia.jugadores.dto.PuntoDto;
import cl.aprendoia.jugadores.exception.RecursoNoEncontradoException;
import cl.aprendoia.jugadores.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Habla con ms-puntos. Traduce sus errores a excepciones propias de este servicio. */
@Component
public class PuntosClient {

    private final RestClient restClient;

    public PuntosClient(RestClient.Builder builder, @Value("${ms-puntos.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public PuntoDto obtenerPunto(Long id) {
        try {
            return restClient.get()
                    .uri("/api/puntos/{id}", id)
                    .retrieve()
                    .body(PuntoDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("No existe el punto con id " + id);
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleException("El servicio de puntos no está disponible");
        }
    }
}