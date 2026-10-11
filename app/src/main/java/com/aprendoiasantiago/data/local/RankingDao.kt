package com.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RankingDao {

    @Query("SELECT * FROM ranking ORDER BY puntos DESC")
    fun observarRanking(): Flow<List<RankingEntity>>

    @Query("SELECT COUNT(*) FROM ranking")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(lista: List<RankingEntity>)
}