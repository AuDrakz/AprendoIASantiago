package com.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {

    @Query("SELECT * FROM perfil WHERE id = :id")
    fun observarPerfil(id: Int): Flow<PerfilEntity?>

    @Query(
        """
        SELECT * FROM perfil
        ORDER BY CASE rol WHEN 'ADMINISTRADOR' THEN 0 WHEN 'GENERADOR' THEN 1 ELSE 2 END,
                 nombreUsuario
        """
    )
    fun observarJugadores(): Flow<List<PerfilEntity>>

    @Query("SELECT * FROM perfil WHERE id = :id")
    suspend fun buscarPorId(id: Int): PerfilEntity?

    @Query("SELECT COUNT(*) FROM perfil")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM perfil WHERE LOWER(email) = LOWER(:email) AND id != :excluirId")
    suspend fun contarPorEmail(email: String, excluirId: Int): Int

    @Insert
    suspend fun insertar(perfil: PerfilEntity): Long

    @Update
    suspend fun actualizar(perfil: PerfilEntity)

    @Query("DELETE FROM perfil WHERE id = :id")
    suspend fun eliminar(id: Int)

    // --- Sesión: una sola fila con el jugador activo ---

    @Query("SELECT jugadorId FROM sesion WHERE id = 1")
    fun observarSesion(): Flow<Int?>

    @Query("SELECT jugadorId FROM sesion WHERE id = 1")
    suspend fun sesionActual(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarSesion(sesion: SesionEntity)

    @Query("DELETE FROM sesion")
    suspend fun cerrarSesion()
}