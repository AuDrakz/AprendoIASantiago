
package com.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoDao {

    @Query("SELECT * FROM puntos ORDER BY id")
    fun observarPuntos(): Flow<List<PuntoEntity>>

    @Query("SELECT COUNT(*) FROM puntos")
    suspend fun contar(): Int

    @Query("SELECT * FROM puntos WHERE codigoQR = :codigo LIMIT 1")
    suspend fun buscarPorQr(codigo: String): PuntoEntity?

    @Query("""
        UPDATE puntos
        SET fueVisitado = 1,
            fechaVisita = :fecha,
            altitudVisita = :altitud
        WHERE id = :id AND fueVisitado = 0
    """)
    suspend fun marcarVisitado(
        id: Int,
        fecha: String,
        altitud: Double?
    ): Int

    @Query("UPDATE puntos SET selfieRuta = :ruta WHERE id = :id")
    suspend fun guardarSelfie(id: Int, ruta: String)

    @Query("SELECT selfieRuta FROM puntos WHERE id = :id")
    suspend fun selfieActual(id: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(puntos: List<PuntoEntity>)

    @Update
    suspend fun actualizar(punto: PuntoEntity)
}
