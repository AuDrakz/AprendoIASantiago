package com.aprendoiasantiago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoDao {

    @Query("SELECT * FROM puntos")
    fun observarPuntos(): Flow<List<PuntoEntity>>

    @Query("SELECT * FROM puntos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): PuntoEntity?

    @Query("SELECT * FROM puntos WHERE codigoQR = :qr LIMIT 1")
    suspend fun buscarPorQr(qr: String): PuntoEntity?

    @Query("SELECT COUNT(*) FROM puntos")
    suspend fun contar(): Int

    @Query("SELECT COALESCE(MAX(id), 0) FROM puntos")
    suspend fun maxId(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(punto: PuntoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(puntos: List<PuntoEntity>)

    // Inserta puntos que falten sin sobrescribir visitas ya registradas.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExiste(punto: PuntoEntity): Long

    // Sincroniza únicamente el QR del punto esperado; no borra fecha, altitud ni visita.
    @Query("UPDATE puntos SET codigoQR = :codigoQR WHERE id = :id AND titulo = :titulo")
    suspend fun actualizarCodigoQr(id: Int, titulo: String, codigoQR: String): Int

    @Update
    suspend fun actualizar(punto: PuntoEntity)

    @Query("DELETE FROM puntos WHERE id = :id")
    suspend fun eliminar(id: Int)

    /**
     * Marca el punto como visitado solo si aún no lo estaba.
     * Devuelve el número de filas modificadas (0 si ya estaba visitado).
     */
    @Query(
        """
        UPDATE puntos
        SET fueVisitado = 1,
            fechaVisita = :fecha,
            altitudVisita = :altitud
        WHERE id = :id AND fueVisitado = 0
        """
    )
    suspend fun marcarVisitado(id: Int, fecha: String, altitud: Double?): Int

    @Query("SELECT selfieRuta FROM puntos WHERE id = :puntoId")
    suspend fun selfieActual(puntoId: Int): String?

    @Query("UPDATE puntos SET selfieRuta = :ruta WHERE id = :puntoId")
    suspend fun guardarSelfie(puntoId: Int, ruta: String)
}