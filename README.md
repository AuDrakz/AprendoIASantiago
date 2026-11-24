# Aprendo IA Jugando en Santiago

App Android desarrollada para Triskeledu SpA como proyecto del ramo
Desarrollo de Aplicaciones Móviles (DSY1105), Duoc UC, sección 006.

Los estudiantes recorren puntos turísticos de Santiago, escanean un código QR
en cada lugar, ven una cápsula de video sobre un concepto de inteligencia
artificial y suman puntos en un ranking.

## Integrantes
- Alan Ojeda
- Nayeli Leiva
- Luna Cortes

## Tecnologías
- Kotlin y Jetpack Compose (Material 3)
- Arquitectura MVVM con repositorio
- Room (SQLite) como persistencia y caché local
- Retrofit + OkHttp para consumir la API externa y los microservicios
- Microservicios en Spring Boot (Java 17, Maven, Spring Data JPA, H2)
- Navigation Compose con diseño adaptable (barra inferior / riel lateral)
- CameraX + ML Kit para escanear códigos QR
- osmdroid para el mapa
- Coil para mostrar las fotos de perfil y de validación
- JUnit, MockK y coroutines-test para las pruebas; JaCoCo para la cobertura

## Estructura del proyecto
```
AprendoIASantiago/
├── app/src/main/java/com/aprendoiasantiago/
│   ├── data/local/     Entidades, DAOs y base de datos Room
│   ├── data/remote/    Retrofit: API externa, microservicios, DTOs y mappers
│   ├── model/          Modelos de dominio
│   ├── repository/     Repositorio (fuente única de datos)
│   ├── viewmodel/      ViewModels y estados de la UI
│   ├── navigation/     Rutas y navegación
│   ├── service/        Servicio de ubicación GPS
│   ├── util/           Utilidades (validaciones, seguridad, errores de red)
│   └── ui/             Pantallas, componentes y tema
├── app/src/test/       Pruebas unitarias de la app
├── backend/
│   ├── ms-puntos/      Microservicio de puntos de interés (puerto 8081)
│   └── ms-jugadores/   Microservicio de jugadores, visitas y ranking (puerto 8082)
├── release/            APK firmado
└── docs/capturas/      Capturas de la documentación
```

## Arquitectura
El flujo de datos de la app es:
Room / Microservicios → Repositorio → ViewModel → estado de UI → pantalla.
Las pantallas no hablan con la red ni con la base de datos, solo observan el
estado del ViewModel. Room funciona como caché local y los microservicios son
la fuente de datos compartida.

| ViewModel | Pantallas | Responsabilidad |
|---|---|---|
| `PuntosViewModel` | Lista, mapa, detalle y gestión de puntos | Puntos, filtro y CRUD de puntos. Es el único que siembra y sincroniza los datos iniciales. |
| `PerfilViewModel` | Perfil, edición y ranking | Perfil del usuario y ranking. |
| `EscanerViewModel` | Escáner QR | Validación del QR y registro de visitas. |
| `SesionViewModel` | Acceso, registro y gestión de jugadores | Sesión, registro y CRUD de jugadores. |

Cada ViewModel expone un `EstadoRed` con el estado de carga y