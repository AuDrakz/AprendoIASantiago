package cl.aprendoia.puntos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Punto de interés del catálogo (global, igual para todos los jugadores).
 * Ojo: "visitado" NO va aquí, porque depende del jugador (eso vive en ms-jugadores).
 * Los nombres de los campos coinciden con PuntoInteres de la app Kotlin.
 */
@Entity
@Table(name = "puntos")
public class Punto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El concepto de IA es obligatorio")
    private String conceptoIA;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "Latitud fuera de rango")
    @DecimalMax(value = "90.0", message = "Latitud fuera de rango")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "Longitud fuera de rango")
    @DecimalMax(value = "180.0", message = "Longitud fuera de rango")
    private Double longitud;

    @NotBlank(message = "El código QR es obligatorio")
    @Column(unique = true)
    private String codigoQR;

    @NotNull(message = "Los puntos de recompensa son obligatorios")
    @Min(value = 0, message = "Los puntos no pueden ser negativos")
    private Integer puntosRecompensa;

    @NotBlank(message = "La URL del video es obligatoria")
    @Pattern(regexp = "^https?://.+", message = "La URL debe comenzar con http:// o https://")
    private String urlVideo;

    public Punto() {
    }

    public Punto(String titulo, String conceptoIA, Double latitud, Double longitud,
                 String codigoQR, Integer puntosRecompensa, String urlVideo) {
        this.titulo = titulo;
        this.conceptoIA = conceptoIA;
        this.latitud = latitud;
        this.longitud = longitud;
        this.codigoQR = codigoQR;
        this.puntosRecompensa = puntosRecompensa;
        this.urlVideo = urlVideo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConceptoIA() { return conceptoIA; }
    public void setConceptoIA(String conceptoIA) { this.conceptoIA = conceptoIA; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getCodigoQR() { return codigoQR; }
    public void setCodigoQR(String codigoQR) { this.codigoQR = codigoQR; }

    public Integer getPuntosRecompensa() { return puntosRecompensa; }
    public void setPuntosRecompensa(Integer puntosRecompensa) { this.puntosRecompensa = puntosRecompensa; }

    public String getUrlVideo() { return urlVideo; }
    public void setUrlVideo(String urlVideo) { this.urlVideo = urlVideo; }
}
