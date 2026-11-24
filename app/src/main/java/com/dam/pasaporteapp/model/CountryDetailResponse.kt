package com.dam.pasaporteapp.model

data class CountryDetailResponse(
    val name: String,
    val nativeName: String? = null,
    val capital: String? = null,
    val region: String? = null,
    val subregion: String? = null,
    val population: Long? = null,
    val area: Double? = null,
    val flags: FlagsResponse? = null,
    val languages: List<LanguageResponse>? = null,
    val currencies: List<CurrencyResponse>? = null
)

data class LanguageResponse(
    val name: String? = null
)

data class CurrencyResponse(
    val name: String? = null,
    val symbol: String? = null
)