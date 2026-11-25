package com.dam.pasaporteapp.repository

import com.dam.pasaporteapp.model.Pais
import com.dam.pasaporteapp.network.RetrofitHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.dam.pasaporteapp.model.CountryDetailResponse
import android.content.Context
import androidx.room.withTransaction
import com.dam.pasaporteapp.database.PaisVisitado
import com.dam.pasaporteapp.database.PasaporteDatabase
import kotlinx.coroutines.flow.Flow

class CountryRepository(contexto: Context) {

    private val baseDeDatos = PasaporteDatabase.obtenerInstancia(contexto)
    private val paisVisitadoDao = baseDeDatos.paisVisitadoDao()

    private var codigosVisitados: Set<String> = emptySet()

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

    suspend fun getCountryByCode(codigo: String): CountryDetailResponse =
        withContext(Dispatchers.IO) {
            RetrofitHelper.apiService.getCountryByCode(codigo)
        }

    suspend fun getQuizCountries(): List<Pais> = withContext(Dispatchers.IO) {
        RetrofitHelper.apiService.getIndependentCountries()
            .mapNotNull { country ->
                val bandera = country.flags?.png
                    ?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null

                Pais(
                    codigo = country.alpha2Code,
                    nombre = country.name,
                    capital = null,
                    region = null,
                    urlBandera = bandera
                )
            }
            .distinctBy { it.codigo }
    }

    suspend fun estaVisitado(codigo: String): Boolean {
        return paisVisitadoDao.buscarPorCodigo(codigo) != null
    }

    suspend fun cambiarEstadoVisita(
        codigo: String,
        pais: CountryDetailResponse
    ): Boolean = baseDeDatos.withTransaction {
        val guardado = paisVisitadoDao.buscarPorCodigo(codigo)

        if (guardado == null) {
            paisVisitadoDao.insertar(pais.toEntity(codigo))
            true
        } else {
            paisVisitadoDao.eliminar(guardado)
            false
        }
    }

    private fun CountryDetailResponse.toEntity(codigo: String): PaisVisitado {
        return PaisVisitado(
            code = codigo,
            name = name,
            capital = capital.orEmpty(),
            region = region.orEmpty(),
            flagUrl = flags?.png.orEmpty()
        )
    }

    fun observarVisitados(): Flow<List<PaisVisitado>> {
        return paisVisitadoDao.observarVisitados()
    }

    suspend fun guardarNota(pais: PaisVisitado, nota: String) {
        paisVisitadoDao.actualizar(pais.copy(note = nota.trim()))
    }

    suspend fun eliminarVisitado(pais: PaisVisitado) {
        paisVisitadoDao.eliminar(pais)
    }

    fun observarCodigosVisitados(): Flow<List<String>> {
        return paisVisitadoDao.observarCodigosVisitados()
    }
}