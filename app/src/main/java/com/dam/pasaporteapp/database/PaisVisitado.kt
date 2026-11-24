package com.dam.pasaporteapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visited_country_table")
data class PaisVisitado(
    @PrimaryKey val code: String,
    val name: String,
    val capital: String,
    val region: String,
    val flagUrl: String,
    val note: String = ""
)