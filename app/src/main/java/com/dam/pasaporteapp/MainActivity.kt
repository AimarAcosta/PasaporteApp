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
import androidx.appcompat.widget.SearchView
import com.dam.pasaporteapp.model.Pais
import android.content.Intent
import com.dam.pasaporteapp.ui.DetallePaisActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var paisAdapter: PaisAdapter
    private val countryRepository by lazy { CountryRepository(applicationContext) }

    private var todosLosPaises: List<Pais>? = null

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
        initFiltros()
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
            val intent = Intent(this, DetallePaisActivity::class.java)
                .putExtra(DetallePaisActivity.EXTRA_CODE, pais.codigo)

            startActivity(intent)
        }

        binding.rvPaises.layoutManager = GridLayoutManager(this, 2)
        binding.rvPaises.adapter = paisAdapter
    }

    private fun initFiltros() {
        binding.searchPaises.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String?): Boolean {
                    aplicarFiltros()
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    binding.searchPaises.clearFocus()
                    return true
                }
            }
        )

        binding.chipGroupRegiones.setOnCheckedStateChangeListener { _, _ ->
            aplicarFiltros()
        }
    }

    private fun aplicarFiltros() {
        val paises = todosLosPaises ?: return
        val busqueda = binding.searchPaises.query.toString().trim()

        val regionSeleccionada = when (binding.chipGroupRegiones.checkedChipId) {
            R.id.chipAfrica -> getString(R.string.region_africa)
            R.id.chipAmericas -> getString(R.string.region_americas)
            R.id.chipAsia -> getString(R.string.region_asia)
            R.id.chipEurope -> getString(R.string.region_europe)
            R.id.chipOceania -> getString(R.string.region_oceania)
            else -> null
        }

        val paisesFiltrados = paises.filter { pais ->
            val coincideNombre = pais.nombre.contains(busqueda, ignoreCase = true)
            val coincideRegion = regionSeleccionada == null ||
                    pais.region == regionSeleccionada

            coincideNombre && coincideRegion
        }

        paisAdapter.actualizarPaises(paisesFiltrados)

        if (paisesFiltrados.isEmpty()) {
            Toast.makeText(this, R.string.sin_resultados_paises, Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarPaises() {
        binding.progressPaises.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                todosLosPaises = countryRepository.getAllCountries()
                aplicarFiltros()
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