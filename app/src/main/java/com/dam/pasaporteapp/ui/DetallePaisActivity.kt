package com.dam.pasaporteapp.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dam.pasaporteapp.R
import com.dam.pasaporteapp.databinding.ActivityDetallePaisBinding
import com.dam.pasaporteapp.model.CountryDetailResponse
import com.dam.pasaporteapp.repository.CountryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import android.content.res.ColorStateList

class DetallePaisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallePaisBinding
    private val countryRepository by lazy { CountryRepository(applicationContext) }

    private val formatoNumero = DecimalFormat(
        "#,##0.##",
        DecimalFormatSymbols(Locale.forLanguageTag("es-ES"))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val codigo = intent.getStringExtra(EXTRA_CODE)
        if (codigo.isNullOrBlank()) {
            finish()
            return
        }

        binding = ActivityDetallePaisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        cargarDetalle(codigo)
    }

    private fun cargarDetalle(codigo: String) {
        lifecycleScope.launch {
            try {
                val pais = countryRepository.getCountryByCode(codigo)
                mostrarDetalle(pais)
                configurarBotonVisitado(codigo, pais)
                binding.contenidoDetalle.isVisible = true
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("PasaporteApp", "Error al cargar el país $codigo", error)
                Toast.makeText(
                    this@DetallePaisActivity,
                    R.string.error_cargar_detalle,
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressDetalle.isVisible = false
            }
        }
    }

    private suspend fun configurarBotonVisitado(
        codigo: String,
        pais: CountryDetailResponse
    ) {
        mostrarEstadoVisitado(countryRepository.estaVisitado(codigo))
        binding.fabVisitado.isVisible = true

        binding.fabVisitado.setOnClickListener {
            binding.fabVisitado.isEnabled = false

            lifecycleScope.launch {
                try {
                    val visitado = countryRepository.cambiarEstadoVisita(codigo, pais)
                    mostrarEstadoVisitado(visitado)

                    val mensaje = if (visitado) {
                        R.string.pais_guardado
                    } else {
                        R.string.pais_eliminado
                    }

                    Toast.makeText(this@DetallePaisActivity, mensaje, Toast.LENGTH_SHORT).show()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Log.e("PasaporteApp", "Error al actualizar la visita de $codigo", error)
                    Toast.makeText(
                        this@DetallePaisActivity,
                        R.string.error_cambiar_visita,
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    binding.fabVisitado.isEnabled = true
                }
            }
        }
    }

    private fun mostrarEstadoVisitado(visitado: Boolean) {
        val icono = if (visitado) {
            android.R.drawable.star_big_on
        } else {
            android.R.drawable.star_big_off
        }

        val colorIcono = if (visitado) {
            R.color.visita_icono_guardado
        } else {
            R.color.visita_icono_no_guardado
        }

        val colorFondo = if (visitado) {
            R.color.visita_fondo_guardado
        } else {
            R.color.visita_fondo_no_guardado
        }

        val descripcion = if (visitado) {
            R.string.desmarcar_visitado
        } else {
            R.string.marcar_visitado
        }

        binding.fabVisitado.apply {
            setImageResource(icono)
            imageTintList = ColorStateList.valueOf(getColor(colorIcono))
            backgroundTintList = ColorStateList.valueOf(getColor(colorFondo))
            contentDescription = getString(descripcion)
        }
    }

    private fun mostrarDetalle(pais: CountryDetailResponse) {
        binding.tvNombreDetalle.text = pais.name
        binding.tvNombreNativo.text = pais.nativeName.orEmpty()
        binding.tvNombreNativo.isVisible = !pais.nativeName.isNullOrBlank()

        Glide.with(this)
            .load(pais.flags?.png)
            .placeholder(R.drawable.ic_quiz)
            .error(R.drawable.ic_quiz)
            .into(binding.ivBanderaDetalle)

        val capital = pais.capital?.takeIf { it.isNotBlank() }
            ?: getString(R.string.no_capital)

        val poblacion = pais.population?.let {
            getString(R.string.detalle_habitantes, formatoNumero.format(it))
        }

        val superficie = pais.area?.let {
            getString(R.string.detalle_km2, formatoNumero.format(it))
        }

        val idiomas = pais.languages.orEmpty()
            .mapNotNull { it.name?.takeIf { nombre -> nombre.isNotBlank() } }
            .joinToString(", ")

        val monedas = pais.currencies.orEmpty().mapNotNull { moneda ->
            val nombre = moneda.name?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null

            if (moneda.symbol.isNullOrBlank()) {
                nombre
            } else {
                getString(R.string.detalle_moneda_con_simbolo, nombre, moneda.symbol)
            }
        }.joinToString(", ")

        binding.tvDatosDetalle.text = listOf(
            getString(R.string.detalle_capital, capital),
            getString(R.string.detalle_region, textoOAlternativa(pais.region)),
            getString(R.string.detalle_subregion, textoOAlternativa(pais.subregion)),
            getString(R.string.detalle_poblacion, textoOAlternativa(poblacion)),
            getString(R.string.detalle_superficie, textoOAlternativa(superficie)),
            getString(R.string.detalle_idiomas, textoOAlternativa(idiomas)),
            getString(R.string.detalle_monedas, textoOAlternativa(monedas))
        ).joinToString("\n")
    }

    private fun textoOAlternativa(texto: String?): String =
        texto?.takeIf { it.isNotBlank() } ?: getString(R.string.detalle_sin_datos)

    companion object {
        const val EXTRA_CODE = "EXTRA_CODE"
    }
}