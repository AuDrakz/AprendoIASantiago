package com.aprendoiasantiago.navigation

sealed class Screen(val route: String) {

    data object ListaPuntos : Screen("lista_puntos")

    data object Mapa : Screen("mapa")

    data object ScannerQR : Screen("scanner_qr")

    data object Perfil : Screen("perfil")

    data object EditarPerfil : Screen("editar_perfil")

    data object DetalleVideo : Screen("detalle_video/{puntoId}") {

        const val ARG_PUNTO_ID = "puntoId"

        fun crearRuta(puntoId: Int) =
            "detalle_video/$puntoId"
    }
}