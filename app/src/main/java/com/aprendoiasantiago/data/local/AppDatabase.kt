package com.aprendoiasantiago.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore

@Database(
    entities = [
        PuntoEntity::class,
        PerfilEntity::class,
        RankingEntity::class,
        SesionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun puntoDao(): PuntoDao

    abstract fun perfilDao(): PerfilDao

    abstract fun rankingDao(): RankingDao

    companion object {

        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aprendoia.db"
                )
                    .fallbackToDestructiveMigration(
                        dropAllTables = true
                    )
                    .build()
                    .also {
                        instancia = it
                    }
            }
        }
    }
}