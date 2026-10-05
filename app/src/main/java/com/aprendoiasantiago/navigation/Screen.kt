package com.aprendoiasantiago.navigation

/**
 * Todas las rutas de la app en un solo lugar.
 * Usar una sealed class evita escribir textos de ruta a mano
 * (y equivocarse) en el resto del código.
 */
sealed class Screen(val route: String) {
    data object ListaPuntos : Screen("lista_puntos")
    data object Mapa : Screen("mapa")
    data object ScannerQR : Screen("scanner_qr")
    data object Perfil : Screen("perfil")

    // Ruta con argumento: recibe el id del punto cuyo video se verá
    data object DetalleVideo : Screen("detalle_video/{puntoId}") {
        const val ARG_PUNTO_ID = "puntoId"
        fun crearRuta(puntoId: Int) = "detalle_video/$puntoId"
    }
}