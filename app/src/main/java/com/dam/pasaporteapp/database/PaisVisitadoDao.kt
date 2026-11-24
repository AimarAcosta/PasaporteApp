package com.dam.pasaporteapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PaisVisitadoDao {

    @Query("SELECT * FROM visited_country_table WHERE code = :codigo")
    suspend fun buscarPorCodigo(codigo: String): PaisVisitado?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pais: PaisVisitado)

    @Delete
    suspend fun eliminar(pais: PaisVisitado)
}