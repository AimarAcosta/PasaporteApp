package com.dam.pasaporteapp.network

import com.dam.pasaporteapp.model.CountryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CountryApiService {

    @GET("countries")
    suspend fun getAllCountries(
        @Query("fields") fields: String = "alpha2Code,name,capital,region,flags"
    ): Response<List<CountryResponse>>
}