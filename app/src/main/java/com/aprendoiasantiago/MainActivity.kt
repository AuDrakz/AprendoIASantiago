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
import androidx.lifecycle.lifecycleScope
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
import com.aprendoiasantiago.viewmodel.SesionViewModel
import android.view.View
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repositorio by lazy {
        val baseDeDatos = AppDatabase.obtener(applicationContext)
        PuntoRepository(
            baseDeDatos.puntoDao(),
            baseDeDatos.perfilDao(),
            baseDeDatos.rankingDao(),
            baseDeDatos.sesionDao(),
            baseDeDatos.visitaDao()
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

            // NUEVO: el permiso llega después del primer intento de sincronización, así que se repite
            if (permisos[PERMISO_RED_LOCAL] == true) {
                lifecycleScope.launch {
                    try {
                        repositorio.sincronizarPuntos()
                        repositorio.sincronizarRanking()
                    } catch (e: Exception) {
                        // sin conexión: la app sigue con lo guardado en Room
                    }
                }
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

    // NUEVO: Android 17 (API 37) bloquea la red local por defecto, incluido el 10.0.2.2
    // del emulador. Hace falta el permiso para que la app llegue al backend.
    private fun necesitaPermisoRedLocal(): Boolean =
        Build.VERSION.SDK_INT >= API_RED_LOCAL &&
                ContextCompat.checkSelfPermission(this, PERMISO_RED_LOCAL) !=
                PackageManager.PERMISSION_GRANTED

    private fun solicitarPermisosEIniciarServicio() {
        val permisos = mutableListOf<String>()

        if (!tienePermisoUbicacion()) {
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permisos.add(Manifest.permission.ACCESS_COARSE_LOCATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permisos.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // NUEVO
        if (necesitaPermisoRedLocal()) {
            permisos.add(PERMISO_RED_LOCAL)
        }

        if (permisos.isEmpty()) {
            iniciarServicioUbicacion()
            return
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
        window.decorView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        enableEdgeToEdge()

        solicitarPermisosEIniciarServicio()

        setContent {
            val altoContraste by userPreferencesRepository.modoAltoContraste
                .collectAsStateWithLifecycle(initialValue = false)

            val daltonismo by userPreferencesRepository.modoDaltonismo
                .collectAsStateWithLifecycle(initialValue = false)
            val modoOscuro by userPreferencesRepository.modoOscuro
                .collectAsStateWithLifecycle(initialValue = true)

            AprendoIASantiagoTheme(
                darkTheme = modoOscuro,
                altoContraste = false,
                daltonismo = daltonismo
            ) {
                val fabrica = AprendoIaViewModelFactory(repositorio)

                val puntosViewModel: PuntosViewModel =
                    viewModel(factory = fabrica)

                val perfilViewModel: PerfilViewModel =
                    viewModel(factory = fabrica)

                val escanerViewModel: EscanerViewModel =
                    viewModel(factory = fabrica)

                val sesionViewModel: SesionViewModel =
                    viewModel(factory = fabrica)

                AppNavigation(
                    puntosViewModel = puntosViewModel,
                    perfilViewModel = perfilViewModel,
                    escanerViewModel = escanerViewModel,
                    sesionViewModel = sesionViewModel,
                    userPreferencesRepository = userPreferencesRepository,
                    windowSizeClass =
                        androidx.compose.material3.windowsizeclass
                            .calculateWindowSizeClass(this@MainActivity)
                )
            }
        }
    }

    // NUEVO
    private companion object {
        const val PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK"
        const val API_RED_LOCAL = 37
    }
}