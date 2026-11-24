package com.dam.pasaporteapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dam.pasaporteapp.R
import com.dam.pasaporteapp.adapter.PasaporteAdapter
import com.dam.pasaporteapp.databinding.ActivityPasaporteBinding
import com.dam.pasaporteapp.repository.CountryRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.app.Dialog
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.dam.pasaporteapp.database.PaisVisitado
import kotlinx.coroutines.CancellationException

class PasaporteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasaporteBinding
    private val countryRepository by lazy { CountryRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPasaporteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        val pasaporteAdapter = PasaporteAdapter(
            onPaisClick = { pais ->
                val intent = Intent(this, DetallePaisActivity::class.java)
                    .putExtra(DetallePaisActivity.EXTRA_CODE, pais.code)

                startActivity(intent)
            },
            onEditarNota = ::editarNota,
            onQuitarPais = ::quitarPais
        )

        binding.rvVisitados.layoutManager = LinearLayoutManager(this)
        binding.rvVisitados.adapter = pasaporteAdapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                countryRepository.observarVisitados().collect { visitados ->
                    pasaporteAdapter.submitList(visitados)

                    val cantidad = visitados.size
                    binding.tvResumenPasaporte.text = resources.getQuantityString(
                        R.plurals.pasaporte_resumen,
                        cantidad,
                        cantidad
                    )
                    binding.tvPasaporteVacio.isVisible = visitados.isEmpty()
                    binding.rvVisitados.isVisible = visitados.isNotEmpty()
                }
            }
        }
    }

    private fun editarNota(pais: PaisVisitado) {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.dialog_note)

        val campoNota = dialogo.findViewById<EditText>(R.id.etNotaPasaporte)
        val botonGuardar = dialogo.findViewById<Button>(R.id.btnGuardarNota)
        val botonCancelar = dialogo.findViewById<Button>(R.id.btnCancelarNota)

        campoNota.setText(pais.note)
        campoNota.setSelection(campoNota.length())

        botonCancelar.setOnClickListener {
            dialogo.dismiss()
        }

        botonGuardar.setOnClickListener {
            val nota = campoNota.text.toString()
            botonGuardar.isEnabled = false

            lifecycleScope.launch {
                try {
                    countryRepository.guardarNota(pais, nota)
                    dialogo.dismiss()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    botonGuardar.isEnabled = true
                    Toast.makeText(
                        this@PasaporteActivity,
                        R.string.error_cambiar_visita,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        dialogo.show()
        dialogo.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun quitarPais(pais: PaisVisitado) {
        lifecycleScope.launch {
            try {
                countryRepository.eliminarVisitado(pais)
                Toast.makeText(
                    this@PasaporteActivity,
                    R.string.pais_eliminado,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Toast.makeText(
                    this@PasaporteActivity,
                    R.string.error_cambiar_visita,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}