package cl.aprendoia.jugadores.client;

import cl.aprendoia.jugadores.dto.PuntoDto;
import cl.aprendoia.jugadores.exception.RecursoNoEncontradoException;
import cl.aprendoia.jugadores.exception.ServicioNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Prueba el cliente HTTP sin levantar ms-puntos: MockRestServiceServer
 * intercepta la petición y responde lo que le indiquemos.
 */
class PuntosClientTest {

    private static final String BASE_URL = "http://localhost:8081";

    private MockRestServiceServer servidor;
    private PuntosClient cliente;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new PuntosClient(builder, BASE_URL);
    }

    @Test
    void obtenerPunto_devuelveElPuntoCuandoMsPuntosResponde200() {
        servidor.expect(requestTo(BASE_URL + "/api/puntos/7"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":7,\"titulo\":\"Plaza\",\"puntosRecompensa\":10}",
                        MediaType.APPLICATION_JSON));

        PuntoDto punto = cliente.obtenerPunto(7L);

        assertEquals(7L, punto.id());
        assertEquals(10, punto.puntosRecompensa());
        servidor.verify();
    }

    @Test
    void obtenerPunto_lanzaNoEncontradoSiMsPuntosResponde404() {
        servidor.expect(requestTo(BASE_URL + "/api/puntos/99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(RecursoNoEncontradoException.class, () -> cliente.obtenerPunto(99L));
    }

    @Test
    void obtenerPunto_lanzaServicioNoDisponibleSiMsPuntosResponde500() {
        servidor.expect(requestTo(BASE_URL + "/api/puntos/7"))
                .andRespond(withServerError());

        assertThrows(ServicioNoDisponibleException.class, () -> cliente.obtenerPunto(7L));
    }

    @Test
    void obtenerPunto_lanzaServicioNoDisponibleSiNoHayConexion() {
        servidor.expect(requestTo(BASE_URL + "/api/puntos/7"))
                .andRespond(withException(new IOException("sin conexión")));

        assertThrows(ServicioNoDisponibleException.class, () -> cliente.obtenerPunto(7L));
    }
}