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
mvn test                                    # pruebas unitarias (en cada carpeta)
```
Consola de la BD: `http://localhost:8081/h2-console` y `:8082/h2-console`
(JDBC URL `jdbc:h2:file:./data/puntosdb`, usuario `sa`, sin clave).

Desde el emulador de Android, el PC se ve como `http://10.0.2.2:8081/` y `:8082`.
Desde un teléfono físico, usa la IP del PC en la misma red wifi.

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
| POST | `/api/jugadores/{id}/visitas` | Registra visita y suma puntos | 201, 400, 404, 409 (ya visitado) |
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
  -d '{"puntoId":1,"puntos":10}'
```

## Decisiones de diseño (para la defensa)
- **Capas:** controller (HTTP) → service (reglas de negocio) → repository (JPA). Es el equivalente a ViewModel → Repository → DAO de la app.
- **Por qué dos microservicios:** el catálogo de puntos casi no cambia y lo lee toda la app; los jugadores y el ranking cambian con cada visita. Cada uno escala y se modifica por separado.
- **"Visitado" no está en `Punto`:** es un dato por jugador, por eso vive en la tabla `visitas` de ms-jugadores (restricción única jugador+punto, igual que la regla actual de no sumar dos veces).
- **Visita + puntos en una transacción** (`@Transactional`): se guardan las dos cosas o ninguna.
- **Pruebas:** JUnit 5 + Mockito sobre los servicios; el repositorio se mockea, así que no necesitan base de datos.

## Pendiente
- Contraseñas y login: hoy siguen en la app (Room). Decidir si pasan al backend.
- ms-jugadores confía en los `puntos` que manda la app; lo más sólido es que consulte a ms-puntos (RestClient) por la recompensa real del punto.
- Probar con MySQL, y agregar tests de controller (`@WebMvcTest`).
