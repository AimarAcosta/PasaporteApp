package com.dam.pasaporteapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PaisVisitado::class],
    version = 1,
    exportSchema = false
)
abstract class PasaporteDatabase : RoomDatabase() {

    abstract fun paisVisitadoDao(): PaisVisitadoDao

    companion object {
        @Volatile
        private var instancia: PasaporteDatabase? = null

        fun obtenerInstancia(contexto: Context): PasaporteDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    PasaporteDatabase::class.java,
                    "pasaporte.db"
                ).build().also { instancia = it }
            }
        }
    }
}