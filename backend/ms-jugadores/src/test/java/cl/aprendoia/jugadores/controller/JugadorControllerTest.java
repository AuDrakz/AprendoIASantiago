package cl.aprendoia.jugadores.controller;

import cl.aprendoia.jugadores.dto.EntradaRanking;
import cl.aprendoia.jugadores.dto.VisitaRequest;
import cl.aprendoia.jugadores.exception.ConflictoException;
import cl.aprendoia.jugadores.exception.RecursoNoEncontradoException;
import cl.aprendoia.jugadores.exception.ServicioNoDisponibleException;
import cl.aprendoia.jugadores.model.Jugador;
import cl.aprendoia.jugadores.model.Rol;
import cl.aprendoia.jugadores.model.Visita;
import cl.aprendoia.jugadores.service.JugadorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
 * Prueba solo la capa HTTP de ms-jugadores: el servicio es un mock,
 * así que no hay base de datos ni llamada real a ms-puntos.
 */
@WebMvcTest(JugadorController.class)
class JugadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JugadorService servicio;

    private static final String JUGADOR_VALIDO = """
            {
              "nombreUsuario": "ana",
              "email": "ana@aprendoia.cl",
              "telefono": "123",
              "rol": "CLIENTE"
            }
            """;

    private static final String JUGADOR_EMAIL_INVALIDO = """
            {
              "nombreUsuario": "ana",
              "email": "correo-malo",
              "telefono": "123",
              "rol": "CLIENTE"
            }
            """;

    private static final String VISITA_VALIDA = """
            { "puntoId": 1 }
            """;

    private Jugador jugador(String nombre, int puntos) {
        return new Jugador(nombre, nombre + "@aprendoia.cl", "", Rol.CLIENTE, puntos);
    }

    // 1
    @Test
    void listar_devuelve200ConLaLista() throws Exception {
        when(servicio.listar()).thenReturn(List.of(jugador("ana", 0)));

        mockMvc.perform(get("/api/jugadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("ana"));
    }

    // 2
    @Test
    void obtener_devuelve404SiNoExiste() throws Exception {
        when(servicio.obtener(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe el jugador con id 99"));

        mockMvc.perform(get("/api/jugadores/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe el jugador con id 99"));
    }

    // 3
    @Test
    void crear_devuelve201ConUnJugadorValido() throws Exception {
        when(servicio.crear(any(Jugador.class))).thenReturn(jugador("ana", 0));

        mockMvc.perform(post("/api/jugadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JUGADOR_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreUsuario").value("ana"))
                .andExpect(jsonPath("$.puntos").value(0));
    }

    // 4
    @Test
    void crear_devuelve400ConEmailInvalidoYNoLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/api/jugadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JUGADOR_EMAIL_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos inválidos"))
                .andExpect(jsonPath("$.detalles").isNotEmpty());

        verify(servicio, never()).crear(any());
    }

    // 5
    @Test
    void crear_devuelve409SiElUsuarioYaExiste() throws Exception {
        when(servicio.crear(any(Jugador.class)))
                .thenThrow(new ConflictoException("El nombre de usuario ya está en uso"));

        mockMvc.perform(post("/api/jugadores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JUGADOR_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409));
    }

    // 6
    @Test
    void registrarVisita_devuelve201ConLosPuntosActualizados() throws Exception {
        when(servicio.registrarVisita(eq(4L), any(VisitaRequest.class)))
                .thenReturn(jugador("matias", 310));

        mockMvc.perform(post("/api/jugadores/4/visitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VISITA_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.puntos").value(310));
    }

    // 7
    @Test
    void registrarVisita_devuelve400SiFaltaElPuntoId() throws Exception {
        mockMvc.perform(post("/api/jugadores/4/visitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos inválidos"));

        verify(servicio, never()).registrarVisita(any(), any());
    }

    // 8
    @Test
    void registrarVisita_devuelve503SiMsPuntosNoEstaDisponible() throws Exception {
        when(servicio.registrarVisita(eq(4L), any(VisitaRequest.class)))
                .thenThrow(new ServicioNoDisponibleException("El servicio de puntos no está disponible"));

        mockMvc.perform(post("/api/jugadores/4/visitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VISITA_VALIDA))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.estado").value(503))
                .andExpect(jsonPath("$.mensaje").value("El servicio de puntos no está disponible"));
    }

    // 9
    @Test
    void registrarVisita_devuelve409SiYaVisitoElPunto() throws Exception {
        when(servicio.registrarVisita(eq(4L), any(VisitaRequest.class)))
                .thenThrow(new ConflictoException("El jugador ya visitó este punto"));

        mockMvc.perform(post("/api/jugadores/4/visitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VISITA_VALIDA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El jugador ya visitó este punto"));
    }

    // 10
    @Test
    void ranking_devuelve200ConNombreYPuntos() throws Exception {
        when(servicio.ranking()).thenReturn(List.of(
                new EntradaRanking("Camila R.", 460),
                new EntradaRanking("Matias R.", 300)));

        mockMvc.perform(get("/api/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Camila R."))
                .andExpect(jsonPath("$[0].puntos").value(460))
                .andExpect(jsonPath("$[1].nombre").value("Matias R."));
    }

    // 11
    @Test
    void obtener_devuelve200SiExiste() throws Exception {
        when(servicio.obtener(4L)).thenReturn(jugador("matias", 310));

        mockMvc.perform(get("/api/jugadores/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("matias"))
                .andExpect(jsonPath("$.puntos").value(310));
    }

    // 12
    @Test
    void actualizar_devuelve200ConDatosValidos() throws Exception {
        when(servicio.actualizar(eq(4L), any(Jugador.class))).thenReturn(jugador("ana", 0));

        mockMvc.perform(put("/api/jugadores/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JUGADOR_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("ana"));
    }

    // 13
    @Test
    void actualizar_devuelve404SiElJugadorNoExiste() throws Exception {
        when(servicio.actualizar(eq(99L), any(Jugador.class)))
                .thenThrow(new RecursoNoEncontradoException("No existe el jugador con id 99"));

        mockMvc.perform(put("/api/jugadores/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JUGADOR_VALIDO))
                .andExpect(status().isNotFound());
    }

    // 14
    @Test
    void actualizar_devuelve400ConEmailInvalidoYNoLlamaAlServicio() throws Exception {
        mockMvc.perform(put("/api/jugadores/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JUGADOR_EMAIL_INVALIDO))
                .andExpect(status().isBadRequest());

        verify(servicio, never()).actualizar(any(), any());
    }

    // 15
    @Test
    void eliminar_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/jugadores/4"))
                .andExpect(status().isNoContent());

        verify(servicio).eliminar(4L);
    }

    // 16
    @Test
    void eliminar_devuelve404SiElJugadorNoExiste() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe el jugador con id 99"))
                .when(servicio).eliminar(99L);

        mockMvc.perform(delete("/api/jugadores/99"))
                .andExpect(status().isNotFound());
    }

    // 17
    @Test
    void visitas_devuelve200ConLasVisitasDelJugador() throws Exception {
        when(servicio.visitasDe(4L))
                .thenReturn(List.of(new Visita(4L, 1L, LocalDateTime.of(2026, 11, 21, 12, 0))));

        mockMvc.perform(get("/api/jugadores/4/visitas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jugadorId").value(4))
                .andExpect(jsonPath("$[0].puntoId").value(1));
    }

    // 18
    @Test
    void visitas_devuelve404SiElJugadorNoExiste() throws Exception {
        when(servicio.visitasDe(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe el jugador con id 99"));

        mockMvc.perform(get("/api/jugadores/99/visitas"))
                .andExpect(status().isNotFound());
    }
}