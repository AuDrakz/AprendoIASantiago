package com.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionDao {

    @Query("SELECT jugadorId FROM sesion WHERE id = 1")
    fun observarSesion(): Flow<Int?>

    @Query("SELECT jugadorId FROM sesion WHERE id = 1")
    suspend fun sesionActual(): Int?

    @Query("SELECT * FROM sesion WHERE id = 1 LIMIT 1")
    suspend fun obtenerSesionActiva(): SesionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarSesion(sesion: SesionEntity)

    @Query("DELETE FROM sesion")
    suspend fun cerrarSesion()
}