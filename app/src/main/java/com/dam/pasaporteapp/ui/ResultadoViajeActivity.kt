package com.dam.pasaporteapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dam.pasaporteapp.R
import com.dam.pasaporteapp.databinding.ActivityResultadoViajeBinding
import com.dam.pasaporteapp.model.EstiloViaje
import com.dam.pasaporteapp.model.PresupuestoViaje
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class ResultadoViajeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nombre = intent.getStringExtra(PlanificadorActivity.EXTRA_NOMBRE)
        val total = intent.getDoubleExtra(EXTRA_TOTAL, Double.NaN)

        if (nombre.isNullOrBlank() || !total.isFinite()) {
            finish()
            return
        }

        val binding = ActivityResultadoViajeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        val (categoria, color) = when {
            total <= 1_000 -> R.string.resultado_economico to R.color.resultado_fondo_economico
            total <= 3_000 -> R.string.resultado_moderado to R.color.resultado_fondo_moderado
            else -> R.string.resultado_gran_viaje to R.color.resultado_fondo_gran_viaje
        }

        val idioma = Locale.forLanguageTag("es-ES")
        val formatoEuros = DecimalFormat("#,##0.##", DecimalFormatSymbols(idioma))

        binding.tvTituloResultado.text = getString(R.string.planificador_titulo, nombre)
        binding.tvTotalResultado.text = getString(
            R.string.planificador_total,
            formatoEuros.format(total)
        )
        binding.tvCategoriaResultado.setText(categoria)
        binding.panelResultado.setBackgroundColor(getColor(color))
        binding.tvDesgloseResultado.text = crearDesglose(idioma)

        binding.btnRecalcular.setOnClickListener {
            finish()
        }
    }

    private fun crearDesglose(idioma: Locale): String {
        val region = intent.getStringExtra(PlanificadorActivity.EXTRA_REGION).orEmpty()
        val dias = intent.getIntExtra(EXTRA_DIAS, 1)
        val viajeros = intent.getIntExtra(EXTRA_VIAJEROS, 1)

        val estilo = when (intent.getStringExtra(EXTRA_ESTILO)) {
            EstiloViaje.MOCHILERO.name -> R.string.resultado_mochilero
            EstiloViaje.LUJO.name -> R.string.resultado_lujo
            else -> R.string.resultado_estandar
        }

        val seguro = if (intent.getBooleanExtra(EXTRA_SEGURO, false)) {
            R.string.resultado_seguro_si
        } else {
            R.string.resultado_seguro_no
        }

        return listOf(
            getString(R.string.planificador_dias, dias),
            getString(R.string.planificador_viajeros, viajeros),
            getString(R.string.resultado_estilo, getString(estilo)),
            getString(R.string.resultado_seguro, getString(seguro)),
            getString(
                R.string.planificador_region,
                region.ifBlank { getString(R.string.detalle_sin_datos) },
                String.format(idioma, "%.1f", PresupuestoViaje.multiplicador(region))
            )
        ).joinToString("\n")
    }

    companion object {
        const val EXTRA_TOTAL = "EXTRA_TOTAL"
        const val EXTRA_DIAS = "EXTRA_DIAS"
        const val EXTRA_VIAJEROS = "EXTRA_VIAJEROS"
        const val EXTRA_ESTILO = "EXTRA_ESTILO"
        const val EXTRA_SEGURO = "EXTRA_SEGURO"
    }
}