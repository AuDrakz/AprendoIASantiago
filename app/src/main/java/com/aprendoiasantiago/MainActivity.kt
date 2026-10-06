package com.aprendoiasantiago

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aprendoiasantiago.data.local.AppDatabase
import com.aprendoiasantiago.navigation.AppNavigation
import com.aprendoiasantiago.repository.PuntoRepository
import com.aprendoiasantiago.ui.theme.AprendoIASantiagoTheme
import com.aprendoiasantiago.viewmodel.AprendoIaViewModel

class MainActivity : ComponentActivity() {

    private val repositorio by lazy {
        val baseDeDatos = AppDatabase.obtener(applicationContext)
        PuntoRepository(baseDeDatos.puntoDao(), baseDeDatos.perfilDao())
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AprendoIASantiagoTheme {
                val viewModel: AprendoIaViewModel = viewModel(
                    factory = AprendoIaViewModel.Factory(repositorio)
                )

                AppNavigation(
                    viewModel = viewModel,
                    windowSizeClass =
                        androidx.compose.material3.windowsizeclass
                            .calculateWindowSizeClass(this@MainActivity)
                )
            }
        }
    }
}