package com.aprendoiasantiago.navigation

sealed class Screen(val route: String) {

    // --- Acceso (sin sesión) ---

    data object Bienvenida : Screen("bienvenida")

    data object SeleccionJugador : Screen("seleccion_jugador")

    data object RegistroJugador : Screen("registro_jugador")

    // --- App principal (con sesión) ---

    data object ListaPuntos : Screen("lista_puntos")

    data object Mapa : Screen("mapa")

    data object ScannerQR : Screen("scanner_qr")

    data object Perfil : Screen("perfil")

    data object EditarPerfil : Screen("editar_perfil")

    // --- Vistas por rol (Administrador y Creador de Contenido) ---

    data object GestionPuntos : Screen("gestion_puntos")

    data object GestionJugadores : Screen("gestion_jugadores")

    data object DetalleVideo : Screen("detalle_video/{puntoId}") {

        const val ARG_PUNTO_ID = "puntoId"

        fun crearRuta(puntoId: Int) =
            "detalle_video/$puntoId"
    }
}