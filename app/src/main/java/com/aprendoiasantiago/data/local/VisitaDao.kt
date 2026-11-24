package com.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitaDao {

    @Query("SELECT * FROM visitas WHERE jugadorId = :jugadorId")
    fun observarVisitas(jugadorId: Int): Flow<List<VisitaEntity>>

    @Query("SELECT * FROM visitas WHERE jugadorId = :jugadorId AND puntoId = :puntoId LIMIT 1")
    suspend fun buscar(jugadorId: Int, puntoId: Int): VisitaEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExiste(visita: VisitaEntity): Long

    @Query("SELECT selfieRuta FROM visitas WHERE jugadorId = :jugadorId AND puntoId = :puntoId")
    suspend fun selfieActual(jugadorId: Int, puntoId: Int): String?

    @Query("UPDATE visitas SET selfieRuta = :ruta WHERE jugadorId = :jugadorId AND puntoId = :puntoId")
    suspend fun guardarSelfie(jugadorId: Int, puntoId: Int, ruta: String)

    @Query("SELECT selfieRuta FROM visitas WHERE puntoId = :puntoId AND selfieRuta IS NOT NULL")
    suspend fun selfiesDePunto(puntoId: Int): List<String>

    @Query("DELETE FROM visitas WHERE puntoId = :puntoId")
    suspend fun eliminarPorPunto(puntoId: Int)

    @Query("SELECT selfieRuta FROM visitas WHERE jugadorId = :jugadorId AND selfieRuta IS NOT NULL")
    suspend fun selfiesDeJugador(jugadorId: Int): List<String>

    @Query("DELETE FROM visitas WHERE jugadorId = :jugadorId")
    suspend fun eliminarPorJugador(jugadorId: Int)
}