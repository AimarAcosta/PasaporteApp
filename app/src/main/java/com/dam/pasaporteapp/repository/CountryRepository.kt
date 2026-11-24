package com.dam.pasaporteapp.repository

import com.dam.pasaporteapp.model.Pais
import com.dam.pasaporteapp.network.RetrofitHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class CountryRepository {

    suspend fun getAllCountries(): List<Pais> = withContext(Dispatchers.IO) {
        val respuesta = RetrofitHelper.apiService.getAllCountries()

        if (!respuesta.isSuccessful) {
            throw HttpException(respuesta)
        }

        val paises = respuesta.body()
            ?: throw IOException("La respuesta no contiene países")

        paises.map { country ->
            Pais(
                codigo = country.alpha2Code,
                nombre = country.name,
                capital = country.capital,
                region = country.region,
                urlBandera = country.flags?.png
            )
        }.sortedBy { it.nombre.lowercase() }
    }
}