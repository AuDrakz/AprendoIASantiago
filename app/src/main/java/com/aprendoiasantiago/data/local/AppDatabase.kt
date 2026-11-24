package com.aprendoiasantiago.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PuntoEntity::class,
        PerfilEntity::class,
        RankingEntity::class,
        SesionEntity::class,
        VisitaEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun puntoDao(): PuntoDao

    abstract fun perfilDao(): PerfilDao

    abstract fun rankingDao(): RankingDao

    abstract fun sesionDao(): SesionDao

    abstract fun visitaDao(): VisitaDao

    companion object {

        private val MIGRACION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `visitas` (" +
                            "`jugadorId` INTEGER NOT NULL, " +
                            "`puntoId` INTEGER NOT NULL, " +
                            "`fecha` TEXT NOT NULL, " +
                            "`altitud` REAL, " +
                            "`selfieRuta` TEXT, " +
                            "PRIMARY KEY(`jugadorId`, `puntoId`))"
                )
            }
        }

        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aprendoia.db"
                )
                    .addMigrations(MIGRACION_6_7)
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