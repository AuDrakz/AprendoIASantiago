# Backend - Aprendo IA Jugando en Santiago

Dos microservicios Spring Boot (Java 17, Maven, Spring Data JPA, H2 en archivo).

| Microservicio | Puerto | Responsabilidad | Base de datos |
|---|---|---|---|
| `ms-puntos` | 8081 | Catálogo de puntos de interés (CRUD) | `./data/puntosdb` |
| `ms-jugadores` | 8082 | Jugadores, visitas y ranking | `./data/jugadoresdb` |

Cada microservicio tiene su propia base de datos y se despliega por separado.

## Cómo ejecutar
Requisitos: JDK 17 o superior y Maven (o abrir cada carpeta en IntelliJ / VS Code / Eclipse).

```bash
cd ms-puntos     && mvn spring-boot:run     # http://localhost:8081
cd ms-jugadores  && mvn spring-boot:run     # http://localhost:8082
mvn test                                    # pruebas unitarias + reporte de cobertura (en cada carpeta)
```
Consola de la BD: `http://localhost:8081/h2-console` y `:8082/h2-console`
(JDBC URL `jdbc:h2:file:./data/puntosdb`, usuario `sa`, sin clave).

Desde el emulador de Android, el PC se ve como `http://10.0.2.2:8081/` y `:8082`.
Desde un teléfono físico, usa la IP del PC en la misma red wifi.

Para registrar visitas deben estar corriendo **ambos** servicios, porque ms-jugadores consulta a ms-puntos (URL configurable con `ms-puntos.url` en `application.properties`).

## Endpoints

### ms-puntos (`/api/puntos`)
| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/puntos` | Lista todos los puntos | 200 |
| GET | `/api/puntos/{id}` | Un punto por id | 200, 404 |
| GET | `/api/puntos/qr/{codigoQR}` | Un punto por su código QR | 200, 404 |
| POST | `/api/puntos` | Crea un punto | 201, 400, 409 (QR repetido) |
| PUT | `/api/puntos/{id}` | Actualiza un punto | 200, 400, 404, 409 |
| DELETE | `/api/puntos/{id}` | Elimina un punto | 204, 404 |

### ms-jugadores (`/api`)
| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/jugadores` | Lista jugadores | 200 |
| GET | `/api/jugadores/{id}` | Un jugador | 200, 404 |
| POST | `/api/jugadores` | Crea un jugador (empieza con 0 puntos) | 201, 400, 409 |
| PUT | `/api/jugadores/{id}` | Actualiza datos (no los puntos) | 200, 400, 404, 409 |
| DELETE | `/api/jugadores/{id}` | Elimina jugador y sus visitas | 204, 404 |
| GET | `/api/jugadores/{id}/visitas` | Visitas del jugador | 200, 404 |
| POST | `/api/jugadores/{id}/visitas` | Registra visita (body: `{ "puntoId": 1 }`); la recompensa la consulta a ms-puntos | 201, 400, 404, 409 (ya visitado), 503 (ms-puntos caído) |
| GET | `/api/ranking` | Ranking ordenado por puntos | 200 |

Errores: siempre JSON `{ "estado": 400, "mensaje": "...", "detalles": ["campo: motivo"] }`.

## Pruebas rápidas con curl
```bash
curl http://localhost:8081/api/puntos
curl -X POST http://localhost:8081/api/puntos -H "Content-Type: application/json" -d '{
  "titulo":"Cerro Santa Lucía","conceptoIA":"Aprendizaje Automático",
  "latitud":-33.4410,"longitud":-70.6440,"codigoQR":"QR_SANTALUCIA_04",
  "puntosRecompensa":10,"urlVideo":"https://aprendoia.cl/videos/ml.mp4"}'

curl http://localhost:8082/api/ranking
curl -X POST http://localhost:8082/api/jugadores/3/visitas -H "Content-Type: application/json" \
-d '{"puntoId":1}'
```

## Pruebas y cobertura
| Microservicio | Tests | Cobertura (instrucciones) |
|---|---|---|
| `ms-puntos` | 23 (11 de servicio + 12 de controller) | 84,3 % |
| `ms-jugadores` | 38 (16 de servicio + 18 de controller + 4 del cliente HTTP) | 86,5 % |

- **Servicios:** JUnit 5 + Mockito; el repositorio se mockea, así que no necesitan base de datos.
- **Controllers:** `@WebMvcTest` + MockMvc; validan rutas, JSON, validaciones y códigos 200, 201, 204, 400, 404, 409 y 503 con el servicio mockeado.
- **Cliente HTTP (`PuntosClient`):** `MockRestServiceServer` simula las respuestas de ms-puntos (200, 404, 500 y sin conexión).
- **Cobertura:** se mide con JaCoCo al ejecutar `mvn test`; el reporte queda en `target/site/jacoco/index.html`. Quedan sin cubrir la clase de arranque (`main`), la siembra de datos iniciales (`DatosIniciales`) y los setters de `Visita`, que no tienen lógica de negocio.

## Decisiones de diseño (para la defensa)
- **Capas:** controller (HTTP) → service (reglas de negocio) → repository (JPA). Es el equivalente a ViewModel → Repository → DAO de la app.
- **Por qué dos microservicios:** el catálogo de puntos casi no cambia y lo lee toda la app; los jugadores y el ranking cambian con cada visita. Cada uno escala y se modifica por separado.
- **"Visitado" no está en `Punto`:** es un dato por jugador, por eso vive en la tabla `visitas` de ms-jugadores (restricción única jugador+punto, igual que la regla actual de no sumar dos veces).
- **Visita + puntos en una transacción** (`@Transactional`): se guardan las dos cosas o ninguna.
- **ms-jugadores consulta a ms-puntos** (`RestClient`) para obtener la recompensa real del punto, en vez de confiar en lo que mande la app. Si ms-puntos no responde, devuelve 503 y no guarda la visita (la consulta se hace antes de guardar).
- **Errores uniformes:** un `@RestControllerAdvice` (`ManejadorErrores`) traduce las excepciones del servicio a códigos HTTP (404, 409, 400 de validación y 503).

## Pendiente
- Contraseñas y login: hoy siguen en la app (Room). Decidir si pasan al backend.
- Probar con MySQL en vez de H2.
- Configurar tiempos límite (timeout) en el `RestClient` que llama a ms-puntos: hoy, si ms-puntos tarda en responder, la petición espera sin límite.