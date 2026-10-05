package com.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoDao {

    // Flow: cada vez que la tabla cambia, emite la lista nueva sola.
    // Gracias a esto la UI se actualiza sin que tengamos que pedirlo.
    @Query("SELECT * FROM puntos ORDER BY id")
    fun observarPuntos(): Flow<List<PuntoEntity>>

    @Query("SELECT COUNT(*) FROM puntos")
    suspend fun contar(): Int

    @Query("SELECT * FROM puntos WHERE codigoQR = :codigo LIMIT 1")
    suspend fun buscarPorQr(codigo: String): PuntoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(puntos: List<PuntoEntity>)

    @Update
    suspend fun actualizar(punto: PuntoEntity)
}