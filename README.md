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
- Sergio Carbajal

## Tecnologías
- Kotlin y Jetpack Compose (Material 3)
- Arquitectura MVVM con repositorio
- Room (SQLite) para persistencia local
- Navigation Compose con diseño adaptable (barra inferior / riel lateral)
- CameraX + ML Kit para escanear códigos QR
- osmdroid para el mapa

## Estructura del proyecto
```
app/src/main/java/com/aprendoiasantiago/
├── data/local/     Entidades, DAOs y base de datos Room
├── model/          Modelos de dominio
├── repository/     Repositorio (fuente única de datos)
├── viewmodel/      ViewModels y estados de la UI
├── navigation/     Rutas y navegación
└── ui/             Pantallas, componentes y tema
```

## Arquitectura
El flujo de datos es: Room → DAO → Repositorio → ViewModel → estado de UI → pantalla.
Las pantallas no hablan con la base de datos, solo observan el estado del ViewModel.

| ViewModel | Pantallas | Responsabilidad |
|---|---|---|
| `PuntosViewModel` | Lista, mapa y detalle | Puntos y filtro. Es el único que siembra los datos iniciales. |
| `PerfilViewModel` | Perfil, edición y ranking | Perfil del usuario y ranking. |
| `EscanerViewModel` | Escáner QR | Validación del QR y registro de visitas. |

Los tres se crean una sola vez en `MainActivity` con `AprendoIaViewModelFactory`
y se pasan por parámetro a la navegación.

## Base de datos
Room (SQLite) con tres tablas: `puntos`, `perfil` y `ranking` (datos ficticios).
El registro de una visita es atómico: solo se suman puntos si el punto
todavía no estaba visitado, aunque se escanee dos veces seguidas.

Durante el desarrollo, si se cambia una entidad hay que subir `version` en
`AppDatabase`. La base se recrea con `fallbackToDestructiveMigration`.

## Cómo ejecutarla
1. Clonar el repositorio y abrirlo con Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar en un emulador o teléfono con Android 8.0 (API 26) o superior.
   El escáner QR necesita cámara y permiso para usarla.

## Funcionalidades actuales

- Lista de puntos de interés con filtros (todos, pendientes y visitados).
- Mapa de OpenStreetMap (OSMDroid) centrado en la posición del usuario con caché de tiles para mala conexión.
- Marcadores visuales distintos para puntos visitados y pendientes.
- Diálogo informativo en marcadores con detalles y cálculo en metros de la distancia al punto.
- Escáner de código QR ejecutado en hilo secundario (`Dispatchers.Default`) para no bloquear la interfaz.
- Validación de cercanía: exige estar a menos de 50 metros del punto para otorgar la recompensa.
- Guardado local en Room de la altitud y timestamp (fecha/hora) de cada visita.
- Servicio de ubicación en segundo plano (`Foreground Service`) con permisos independientes.
- Reproducción de video de cápsulas de IA integrada dentro de la app con ExoPlayer.
- Perfil editable con validación de formulario.
- Ranking de estudiantes guardado en base de datos local.
Probada en teléfono físico.

## Convenciones del equipo
- Una rama por funcionalidad (`feature/...`) y Pull Request hacia `master`.
- Los textos de la interfaz van en `strings.xml`, no escritos en el código.
- `.idea/` y las claves de firma (`*.jks`, `*.keystore`) no se suben al repositorio.

## En desarrollo

- Notificaciones automáticas de cercanía a puntos pendientes de visita.
- Optimización avanzada de descarga de mapas sin conexión por áreas.
- Foto de perfil y selfie de validación (cámara y galería).
- Perfiles de usuario: cliente, generador de contenido y administrador.
- Pruebas unitarias.