package com.aprendoiasantiago.viewmodel

import androidx.annotation.StringRes

/** Estado del formulario de edición de perfil. Los errores son ids de strings.xml. */
data class FormularioUiState(
    val nombre: String = "",
    val email: String = "",
    val fotoRuta: String? = null,
    @StringRes val errorNombre: Int? = null,
    @StringRes val errorEmail: Int? = null
)