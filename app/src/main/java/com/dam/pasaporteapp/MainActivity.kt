package com.dam.pasaporteapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.dam.pasaporteapp.adapter.PaisAdapter
import com.dam.pasaporteapp.databinding.ActivityMainBinding
import com.dam.pasaporteapp.repository.CountryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var paisAdapter: PaisAdapter
    private val countryRepository = CountryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        binding.tvTitulo.setText(R.string.titulo_lista_paises)

        initListeners()
        initRecyclerView()
        cargarPaises()
    }

    private fun initListeners() {
        binding.btnInicio.setOnClickListener {
            Toast.makeText(this, R.string.nav_inicio, Toast.LENGTH_SHORT).show()
        }

        binding.btnPasaporte.setOnClickListener {
            Toast.makeText(this, R.string.nav_pasaporte, Toast.LENGTH_SHORT).show()
        }

        binding.btnQuiz.setOnClickListener {
            Toast.makeText(this, R.string.nav_quiz, Toast.LENGTH_SHORT).show()
        }

        binding.btnAjustes.setOnClickListener {
            Toast.makeText(this, R.string.nav_ajustes, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initRecyclerView() {
        paisAdapter = PaisAdapter { pais ->
            Toast.makeText(
                this,
                "Has pulsado ${pais.nombre}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.rvPaises.layoutManager = GridLayoutManager(this, 2)
        binding.rvPaises.adapter = paisAdapter
    }

    private fun cargarPaises() {
        binding.progressPaises.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val paises = countryRepository.getAllCountries()
                paisAdapter.actualizarPaises(paises)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("PasaporteApp", "Error al cargar los países", error)
                Toast.makeText(
                    this@MainActivity,
                    R.string.error_loading_countries,
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressPaises.visibility = View.GONE
            }
        }
    }
}