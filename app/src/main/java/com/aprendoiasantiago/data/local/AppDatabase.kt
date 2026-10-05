package com.aprendoiasantiago.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos SQLite de la app. Debe existir UNA sola instancia
 * (por eso el patrón singleton del companion object).
 */
@Database(
    entities = [PuntoEntity::class, PerfilEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun puntoDao(): PuntoDao
    abstract fun perfilDao(): PerfilDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aprendoia.db"
                ).build().also { instancia = it }
            }
    }
}