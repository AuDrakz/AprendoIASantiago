package com.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// Clase abstracta (y no interface) porque reemplazar() tiene cuerpo con @Transaction
@Dao
abstract class RankingDao {

    @Query("SELECT * FROM ranking ORDER BY puntos DESC")
    abstract fun observarRanking(): Flow<List<RankingEntity>>

    @Query("SELECT COUNT(*) FROM ranking")
    abstract suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarTodos(lista: List<RankingEntity>)

    @Query("DELETE FROM ranking")
    abstract suspend fun borrarTodo()

    /** Reemplaza el ranking completo por el que llegó del servidor, todo o nada. */
    @Transaction
    open suspend fun reemplazar(lista: List<RankingEntity>) {
        borrarTodo()
        insertarTodos(lista)
    }
}