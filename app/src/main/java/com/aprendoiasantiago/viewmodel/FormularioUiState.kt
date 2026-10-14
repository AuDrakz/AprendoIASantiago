package com.aprendoiasantiago.viewmodel

import androidx.annotation.StringRes
import com.aprendoiasantiago.model.Rol

/** Estado del formulario de registro/edición. Los errores son ids de strings.xml. */
data class FormularioUiState(
    val nombre: String = "",
    val email: String = "",
    val rol: Rol = Rol.CLIENTE,
    val fotoRuta: String? = null,
    @StringRes val errorNombre: Int? = null,
    @StringRes val errorEmail: Int? = null
)