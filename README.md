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
- Coil para mostrar las fotos de perfil y de validación

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
- Mapa con la ubicación del usuario y los puntos marcados.
- Escáner de códigos QR que valida el punto y suma los puntos de recompensa.
- Registro de visitas con validación de QR repetido.
- Detalle de cada punto con acceso a su video.
- Perfil editable con validación de formulario.
- Ranking de estudiantes (datos ficticios) guardado en la base de datos local.
- Registro de usuario con rol: cliente, generador de contenido o administrador.
- Formulario con validación en el ViewModel (nombre y correo).
- Foto de perfil desde cámara o galería, guardada en Room y mostrada en el perfil.
- Selfie de validación al visitar un punto, guardada junto al punto en Room.

Probada en teléfono físico.

## Convenciones del equipo
- Una rama por funcionalidad (`feature/...`) y Pull Request hacia `master`.
- Los textos de la interfaz van en `strings.xml`, no escritos en el código.
- `.idea/` y las claves de firma (`*.jks`, `*.keystore`) no se suben al repositorio.

## En desarrollo
- Animaciones y tema de alto contraste.
- Pruebas unitarias.
- Control de acceso por rol (hoy el rol es informativo y se elige libremente) y sesión de usuario.