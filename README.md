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
├── viewmodel/      ViewModel y estado de la UI
├── navigation/     Rutas y navegación
└── ui/             Pantallas, componentes y tema
```

## Cómo ejecutarla
1. Clonar el repositorio y abrirlo con Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar en un emulador o teléfono con Android 8.0 (API 26) o superior.
   El escáner QR necesita cámara y permiso para usarla.

## Funcionalidades actuales
- Lista de puntos de interés con filtros (todos, pendientes y visitados).
- Mapa con la ubicación del usuario y los puntos marcados.
- Escáner de códigos QR que valida el punto y suma los puntos de recompensa.
- Detalle de cada punto con acceso a su video.
- Perfil editable con validación de formulario.
- Ranking de estudiantes guardado en la base de datos local.

Probada en teléfono físico.

## En desarrollo
- Foto de perfil y selfie de validación (cámara y galería).
- Perfiles de usuario: cliente, generador de contenido y administrador.
- Animaciones y tema de alto contraste.
- Pruebas unitarias.
