package cl.aprendoia.jugadores.config;

import cl.aprendoia.jugadores.model.Jugador;
import cl.aprendoia.jugadores.model.Rol;
import cl.aprendoia.jugadores.repository.JugadorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Siembra los jugadores y el ranking de ejemplo de la app, solo si la tabla está vacía. */
@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner sembrarJugadores(JugadorRepository repositorio) {
        return args -> {
            if (repositorio.count() == 0) {
                repositorio.saveAll(List.of(
                        new Jugador("Administrador", "admin@aprendoia.cl", "", Rol.ADMINISTRADOR, 0),
                        new Jugador("Contenido", "contenido@aprendoia.cl", "", Rol.GENERADOR, 0),
                        new Jugador("Camila R.", "camila@aprendoia.cl", "", Rol.CLIENTE, 450),
                        new Jugador("Matias R.", "matias@aprendoia.cl", "", Rol.CLIENTE, 300),
                        new Jugador("Valentina S.", "valentina@aprendoia.cl", "", Rol.CLIENTE, 150)
                ));
            }
        };
    }
}
