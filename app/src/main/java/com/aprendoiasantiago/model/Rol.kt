package com.aprendoiasantiago.model

import androidx.annotation.StringRes
import com.aprendoiasantiago.R

enum class Rol(@StringRes val etiqueta: Int) {
    CLIENTE(R.string.rol_cliente),
    GENERADOR(R.string.rol_generador),
    ADMINISTRADOR(R.string.rol_administrador)
}