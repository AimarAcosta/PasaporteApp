package com.dam.pasaporteapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dam.pasaporteapp.R
import com.dam.pasaporteapp.databinding.ActivityPlanificadorBinding
import com.dam.pasaporteapp.model.EstiloViaje
import com.dam.pasaporteapp.model.PresupuestoViaje
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class PlanificadorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanificadorBinding
    private val idiomaFormato = Locale.forLanguageTag("es-ES")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nombre = intent.getStringExtra(EXTRA_NOMBRE)
        if (nombre.isNullOrBlank()) {
            finish()
            return
        }

        val region = intent.getStringExtra(EXTRA_REGION).orEmpty()

        binding = ActivityPlanificadorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        binding.tvTituloViaje.text = getString(R.string.planificador_titulo, nombre)
        binding.tvRegionViaje.text = getString(
            R.string.planificador_region,
            region.ifBlank { getString(R.string.detalle_sin_datos) },
            String.format(idiomaFormato, "%.1f", PresupuestoViaje.multiplicador(region))
        )

        configurarBarra(binding.seekDias, binding.tvDiasViaje, R.string.planificador_dias)
        configurarBarra(
            binding.seekViajeros,
            binding.tvViajerosViaje,
            R.string.planificador_viajeros
        )

        binding.btnCalcular.setOnClickListener {
            calcularPresupuesto(nombre, region)
        }
    }

    private fun configurarBarra(barra: SeekBar, texto: TextView, recurso: Int) {
        texto.text = getString(recurso, barra.progress + 1)

        barra.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(barra: SeekBar, progreso: Int, desdeUsuario: Boolean) {
                texto.text = getString(recurso, progreso + 1)
            }

            override fun onStartTrackingTouch(barra: SeekBar) = Unit

            override fun onStopTrackingTouch(barra: SeekBar) = Unit
        })
    }

    private fun calcularPresupuesto(nombre: String, region: String) {
        val estilo = when (binding.rgEstilo.checkedRadioButtonId) {
            R.id.rbMochilero -> EstiloViaje.MOCHILERO
            R.id.rbLujo -> EstiloViaje.LUJO
            else -> EstiloViaje.ESTANDAR
        }

        val presupuesto = PresupuestoViaje(
            region = region,
            dias = binding.seekDias.progress + 1,
            viajeros = binding.seekViajeros.progress + 1,
            estilo = estilo,
            conSeguro = binding.switchSeguro.isChecked
        )

        val intent = Intent(this, ResultadoViajeActivity::class.java)
            .putExtra(EXTRA_NOMBRE, nombre)
            .putExtra(EXTRA_REGION, presupuesto.region)
            .putExtra(ResultadoViajeActivity.EXTRA_TOTAL, presupuesto.total)
            .putExtra(ResultadoViajeActivity.EXTRA_DIAS, presupuesto.dias)
            .putExtra(ResultadoViajeActivity.EXTRA_VIAJEROS, presupuesto.viajeros)
            .putExtra(ResultadoViajeActivity.EXTRA_ESTILO, presupuesto.estilo.name)
            .putExtra(ResultadoViajeActivity.EXTRA_SEGURO, presupuesto.conSeguro)

        startActivity(intent)
    }

    companion object {
        const val EXTRA_NOMBRE = "EXTRA_NOMBRE"
        const val EXTRA_REGION = "EXTRA_REGION"
    }
}