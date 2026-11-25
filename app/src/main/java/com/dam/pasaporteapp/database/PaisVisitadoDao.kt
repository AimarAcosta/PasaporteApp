package com.dam.pasaporteapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update

@Dao
interface PaisVisitadoDao {

    @Query("SELECT * FROM visited_country_table WHERE code = :codigo")
    suspend fun buscarPorCodigo(codigo: String): PaisVisitado?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pais: PaisVisitado)

    @Delete
    suspend fun eliminar(pais: PaisVisitado)

    @Query("SELECT * FROM visited_country_table ORDER BY name COLLATE NOCASE")
    fun observarVisitados(): Flow<List<PaisVisitado>>

    @Update
    suspend fun actualizar(pais: PaisVisitado)

    @Query("SELECT code FROM visited_country_table")
    fun observarCodigosVisitados(): Flow<List<String>>
}