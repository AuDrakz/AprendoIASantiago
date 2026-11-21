package cl.aprendoia.puntos.controller;

import cl.aprendoia.puntos.exception.ConflictoException;
import cl.aprendoia.puntos.exception.RecursoNoEncontradoException;
import cl.aprendoia.puntos.model.Punto;
import cl.aprendoia.puntos.service.PuntoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba solo la capa HTTP: el servicio se reemplaza por un mock,
 * así que no hay base de datos ni servidor real.
 */
@WebMvcTest(PuntoController.class)
class PuntoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PuntoService servicio;

    private static final String JSON_VALIDO = """
            {
              "titulo": "Plaza de Armas",
              "conceptoIA": "NLP",
              "latitud": -33.43,
              "longitud": -70.65,
              "codigoQR": "QR_PLAZA_02",
              "puntosRecompensa": 10,
              "urlVideo": "https://aprendoia.cl/v.mp4"
            }
            """;

    private static final String JSON_INVALIDO = """
            {
              "titulo": "",
              "conceptoIA": "NLP",
              "latitud": 200,
              "longitud": -70.65,
              "codigoQR": "QR_X",
              "puntosRecompensa": -5,
              "urlVideo": "abc"
            }
            """;

    private Punto punto() {
        return new Punto("Plaza de Armas", "NLP", -33.43, -70.65,
                "QR_PLAZA_02", 10, "https://aprendoia.cl/v.mp4");
    }

    @Test
    void listar_devuelve200ConLaLista() throws Exception {
        when(servicio.listar()).thenReturn(List.of(punto()));

        mockMvc.perform(get("/api/puntos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Plaza de Armas"));
    }

    @Test
    void obtener_devuelve404SiNoExiste() throws Exception {
        when(servicio.obtener(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe el punto con id 99"));

        mockMvc.perform(get("/api/puntos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe el punto con id 99"));
    }

    @Test
    void crear_devuelve201ConUnPuntoValido() throws Exception {
        when(servicio.crear(any(Punto.class))).thenReturn(punto());

        mockMvc.perform(post("/api/puntos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoQR").value("QR_PLAZA_02"));
    }

    @Test
    void crear_devuelve400ConDatosInvalidosYNoLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/api/puntos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos inválidos"))
                .andExpect(jsonPath("$.detalles").isNotEmpty());

        verify(servicio, never()).crear(any());
    }

    @Test
    void crear_devuelve409SiElQrYaExiste() throws Exception {
        when(servicio.crear(any(Punto.class)))
                .thenThrow(new ConflictoException("Ya existe un punto con el QR QR_PLAZA_02"));

        mockMvc.perform(post("/api/puntos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409));
    }

    @Test
    void eliminar_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/puntos/1"))
                .andExpect(status().isNoContent());

        verify(servicio).eliminar(1L);
    }

    @Test
    void obtener_devuelve200SiExiste() throws Exception {
        when(servicio.obtener(1L)).thenReturn(punto());

        mockMvc.perform(get("/api/puntos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Plaza de Armas"));
    }

    @Test
    void obtenerPorQr_devuelve200SiExiste() throws Exception {
        when(servicio.obtenerPorQr("QR_PLAZA_02")).thenReturn(punto());

        mockMvc.perform(get("/api/puntos/qr/QR_PLAZA_02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoQR").value("QR_PLAZA_02"));
    }

    @Test
    void actualizar_devuelve200ConDatosValidos() throws Exception {
        when(servicio.actualizar(eq(1L), any(Punto.class))).thenReturn(punto());

        mockMvc.perform(put("/api/puntos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntosRecompensa").value(10));
    }

    @Test
    void actualizar_devuelve404SiElPuntoNoExiste() throws Exception {
        when(servicio.actualizar(eq(99L), any(Punto.class)))
                .thenThrow(new RecursoNoEncontradoException("No existe el punto con id 99"));

        mockMvc.perform(put("/api/puntos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizar_devuelve400ConDatosInvalidosYNoLlamaAlServicio() throws Exception {
        mockMvc.perform(put("/api/puntos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_INVALIDO))
                .andExpect(status().isBadRequest());

        verify(servicio, never()).actualizar(any(), any());
    }

    @Test
    void eliminar_devuelve404SiElPuntoNoExiste() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe el punto con id 99"))
                .when(servicio).eliminar(99L);

        mockMvc.perform(delete("/api/puntos/99"))
                .andExpect(status().isNotFound());
    }
}