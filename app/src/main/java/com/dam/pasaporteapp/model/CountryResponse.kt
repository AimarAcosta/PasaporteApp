package com.dam.pasaporteapp.model

data class CountryResponse(
    val alpha2Code: String,
    val name: String,
    val capital: String? = null,
    val region: String? = null,
    val flags: FlagsResponse? = null
)

data class FlagsResponse(
    val png: String? = null,
    val svg: String? = null
)