
package com.aprendoiasantiago

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.aprendoiasantiago.data.local.AppDatabase
import com.aprendoiasantiago.data.local.UserPreferencesRepository
import com.aprendoiasantiago.navigation.AppNavigation
import com.aprendoiasantiago.repository.PuntoRepository
import com.aprendoiasantiago.service.UbicacionService
import com.aprendoiasantiago.ui.theme.AprendoIASantiagoTheme
import com.aprendoiasantiago.viewmodel.AprendoIaViewModelFactory
import com.aprendoiasantiago.viewmodel.EscanerViewModel
import com.aprendoiasantiago.viewmodel.PerfilViewModel
import com.aprendoiasantiago.viewmodel.PuntosViewModel

class MainActivity : ComponentActivity() {

    private val repositorio by lazy {
        val baseDeDatos = AppDatabase.obtener(applicationContext)
        PuntoRepository(
            baseDeDatos.puntoDao(),
            baseDeDatos.perfilDao(),
            baseDeDatos.rankingDao()
        )
    }

    private val userPreferencesRepository by lazy {
        UserPreferencesRepository(applicationContext)
    }

    private val solicitudPermisosUbicacion =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permisos ->

            val ubicacionConcedida =
                permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                        tienePermisoUbicacion()

            if (ubicacionConcedida) {
                iniciarServicioUbicacion()
            }
        }

    private fun tienePermisoUbicacion(): Boolean {
        val permisoFino = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return permisoFino || permisoAproximado
    }

    private fun solicitarPermisosEIniciarServicio() {
        if (tienePermisoUbicacion()) {
            iniciarServicioUbicacion()
            return
        }

        val permisos = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        solicitudPermisosUbicacion.launch(permisos.toTypedArray())
    }

    private fun iniciarServicioUbicacion() {
        if (!tienePermisoUbicacion()) return

        val intent = Intent(
            this,
            UbicacionService::class.java
        ).apply {
            action = UbicacionService.ACCION_INICIAR
        }

        ContextCompat.startForegroundService(this, intent)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        solicitarPermisosEIniciarServicio()

        setContent {
            val altoContraste by userPreferencesRepository.modoAltoContraste
                .collectAsStateWithLifecycle(initialValue = false)

            AprendoIASantiagoTheme(altoContraste = altoContraste) {
                val fabrica = AprendoIaViewModelFactory(repositorio)

                val puntosViewModel: PuntosViewModel =
                    viewModel(factory = fabrica)

                val perfilViewModel: PerfilViewModel =
                    viewModel(factory = fabrica)

                val escanerViewModel: EscanerViewModel =
                    viewModel(factory = fabrica)

                AppNavigation(
                    puntosViewModel = puntosViewModel,
                    perfilViewModel = perfilViewModel,
                    escanerViewModel = escanerViewModel,
                    userPreferencesRepository = userPreferencesRepository,
                    windowSizeClass =
                        androidx.compose.material3.windowsizeclass
                            .calculateWindowSizeClass(this@MainActivity)
                )
            }
        }
    }
}
