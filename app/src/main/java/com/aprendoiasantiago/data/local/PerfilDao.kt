package com.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {

    @Query("SELECT * FROM perfil WHERE id = :id")
    fun observarPerfil(id: Int): Flow<PerfilEntity?>

    @Query("SELECT COUNT(*) FROM perfil")
    suspend fun contar(): Int

    @Query("SELECT fotoRuta FROM perfil WHERE id = :id")
    suspend fun fotoActual(id: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(perfil: PerfilEntity)
}