package com.dam.pasaporteapp.model

data class Pais(
    val codigo: String,
    val nombre: String,
    val capital: String?,
    val region: String?,
    val urlBandera: String? = null
)