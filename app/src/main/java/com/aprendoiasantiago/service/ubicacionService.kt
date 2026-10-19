package com.aprendoiasantiago.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.aprendoiasantiago.MainActivity

class UbicacionService : Service() {

    companion object {
        const val ACCION_INICIAR =
            "com.aprendoiasantiago.INICIAR_UBICACION"

        const val ACCION_DETENER =
            "com.aprendoiasantiago.DETENER_UBICACION"

        private const val CANAL_ID = "ubicacion_gps"
        private const val NOTIFICACION_ID = 1001
        private const val TAG = "UbicacionService"
        private const val INTERVALO_MS = 5000L
        private const val DISTANCIA_MINIMA_METROS = 5f
    }

    private lateinit var locationManager: LocationManager
    private var escuchando = false

    private val listener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            val altitud = if (location.hasAltitude()) {
                location.altitude.toString()
            } else {
                "no disponible"
            }

            Log.d(
                TAG,
                "Ubicación recibida: lat=${location.latitude}, " +
                        "lon=${location.longitude}, alt=$altitud"
            )
        }

        override fun onProviderDisabled(provider: String) {
            Log.d(TAG, "Proveedor desactivado: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.d(TAG, "Proveedor activado: $provider")
        }

        @Deprecated("Método requerido por compatibilidad")
        override fun onStatusChanged(
            provider: String?,
            status: Int,
            extras: Bundle?
        ) {
            // No se necesita realizar ninguna acción aquí.
        }
    }

    override fun onCreate() {
        super.onCreate()

        locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        crearCanalNotificacion()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (intent?.action == ACCION_DETENER) {
            detenerSeguimiento()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        if (!tienePermisoUbicacion()) {
            Log.w(TAG, "No hay permiso de ubicación concedido.")
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            iniciarComoServicioEnPrimerPlano()
            iniciarSeguimiento()
        } catch (e: SecurityException) {
            Log.e(TAG, "No fue posible iniciar el seguimiento GPS.", e)
            detenerSeguimiento()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
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

    private fun iniciarComoServicioEnPrimerPlano() {
        val notificacion = crearNotificacion()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICACION_ID,
                notificacion,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICACION_ID, notificacion)
        }
    }

    private fun iniciarSeguimiento() {
        if (escuchando) return

        if (!tienePermisoUbicacion()) {
            Log.w(TAG, "Permiso de ubicación insuficiente.")
            stopSelf()
            return
        }

        var algunProveedorActivo = false

        try {
            if (
                locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
                )
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    INTERVALO_MS,
                    DISTANCIA_MINIMA_METROS,
                    listener,
                    Looper.getMainLooper()
                )

                algunProveedorActivo = true
                Log.d(TAG, "Seguimiento GPS iniciado.")
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "No se pudo activar el proveedor GPS.", e)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Proveedor GPS no disponible.", e)
        }

        try {
            if (
                locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    INTERVALO_MS,
                    DISTANCIA_MINIMA_METROS,
                    listener,
                    Looper.getMainLooper()
                )

                algunProveedorActivo = true
                Log.d(TAG, "Seguimiento por red iniciado.")
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "No se pudo activar el proveedor de red.", e)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Proveedor de red no disponible.", e)
        }

        escuchando = algunProveedorActivo

        if (!escuchando) {
            Log.w(TAG, "No hay proveedores de ubicación activos.")
        }
    }

    private fun detenerSeguimiento() {
        if (::locationManager.isInitialized) {
            try {
                locationManager.removeUpdates(listener)
            } catch (e: SecurityException) {
                Log.w(TAG, "No se pudieron quitar las actualizaciones GPS.", e)
            }
        }

        escuchando = false
        Log.d(TAG, "Seguimiento GPS detenido.")
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Seguimiento GPS",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación del seguimiento de ubicación"
            }

            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(canal)
        }
    }

    private fun crearNotificacion(): Notification {
        val intent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        0
                    }
        )

        return NotificationCompat.Builder(this, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("AprendoIA Santiago")
            .setContentText("Seguimiento GPS activo")
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun onDestroy() {
        detenerSeguimiento()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}