package cl.aprendoia.puntos.config;

import cl.aprendoia.puntos.model.Punto;
import cl.aprendoia.puntos.repository.PuntoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Siembra los mismos 3 puntos que hoy tiene la app en Room, solo si la tabla está vacía. */
@Configuration
public class DatosIniciales {

    @Bean
    CommandLineRunner sembrarPuntos(PuntoRepository repositorio) {
        return args -> {
            if (repositorio.count() == 0) {
                repositorio.saveAll(List.of(
                        new Punto("Palacio de La Moneda", "Visión por Computador",
                                -33.4429, -70.6539, "QR_LAMONEDA_01", 10,
                                "https://aprendoia.cl/videos/vision_computador.mp4"),
                        new Punto("Plaza de Armas", "Procesamiento de Lenguaje Natural",
                                -33.4378, -70.6504, "QR_PLAZA_02", 10,
                                "https://aprendoia.cl/videos/nlp.mp4"),
                        new Punto("Parque Forestal", "Redes Neuronales",
                                -33.4350, -70.6420, "QR_FORESTAL_03", 10,
                                "https://aprendoia.cl/videos/redes_neuronales.mp4")
                ));
            }
        };
    }
}
