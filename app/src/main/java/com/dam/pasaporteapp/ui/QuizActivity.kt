package com.dam.pasaporteapp.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dam.pasaporteapp.R
import com.dam.pasaporteapp.databinding.ActivityQuizBinding
import com.dam.pasaporteapp.model.Pais
import com.dam.pasaporteapp.repository.CountryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private val countryRepository by lazy { CountryRepository(applicationContext) }

    private lateinit var botones: List<Button>


    private var paisesDisponibles: List<Pais> = emptyList()
    private var paisesPartida: List<Pais> = emptyList()
    private var indicePregunta = 0
    private var aciertos = 0
    private var respondida = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        botones = listOf(
            binding.btnOpcion1,
            binding.btnOpcion2,
            binding.btnOpcion3,
            binding.btnOpcion4
        )

        binding.btnSiguienteQuiz.setOnClickListener {
            if (respondida) avanzar()
        }

        binding.btnRepetirQuiz.setOnClickListener {
            iniciarPartida()
        }

        cargarPaises()
    }

    private fun cargarPaises() {
        lifecycleScope.launch {
            try {
                paisesDisponibles = countryRepository.getQuizCountries()

                if (paisesDisponibles.size < TOTAL_PREGUNTAS) {
                    mostrarError(R.string.quiz_pocos_paises)
                    return@launch
                }

                iniciarPartida()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e("PasaporteApp", "Error al cargar el quiz", error)
                mostrarError(R.string.quiz_error_carga)
            } finally {
                binding.progressQuiz.isVisible = false
            }
        }
    }

    private fun iniciarPartida() {
        paisesPartida = paisesDisponibles.shuffled().take(TOTAL_PREGUNTAS)
        indicePregunta = 0
        aciertos = 0

        binding.resultadoQuiz.isVisible = false
        binding.contenidoQuiz.isVisible = true
        mostrarPregunta()
    }

    private fun mostrarPregunta() {
        respondida = false
        binding.btnSiguienteQuiz.isEnabled = false
        binding.tvRespuestaQuiz.text = ""

        binding.tvProgresoQuiz.text = getString(
            R.string.quiz_progreso,
            indicePregunta + 1,
            TOTAL_PREGUNTAS
        )
        actualizarMarcador()

        val correcto = paisesPartida[indicePregunta]
        val incorrectos = paisesDisponibles
            .filter { it.codigo != correcto.codigo }
            .shuffled()
            .take(3)
        val opciones = (incorrectos + correcto).shuffled()

        Glide.with(this)
            .load(correcto.urlBandera)
            .placeholder(R.drawable.ic_quiz)
            .error(R.drawable.ic_quiz)
            .into(binding.ivBanderaQuiz)

        botones.zip(opciones).forEachIndexed { posicion, (boton, opcion) ->
            boton.text = opcion.nombre
            boton.isEnabled = true
            boton.backgroundTintList = ColorStateList.valueOf(getColor(R.color.quiz_opcion_fondo))
            boton.setTextColor(getColor(R.color.quiz_opcion_texto))

            boton.setOnClickListener {
                comprobarRespuesta(posicion, opciones, correcto)
            }
        }
    }

    private fun comprobarRespuesta(
        posicionElegida: Int,
        opciones: List<Pais>,
        correcto: Pais
    ) {
        if (respondida) return
        respondida = true

        val acierto = opciones[posicionElegida].codigo == correcto.codigo
        if (acierto) aciertos++

        botones.zip(opciones).forEachIndexed { posicion, (boton, opcion) ->
            boton.isEnabled = false

            val colorFondo = when {
                opcion.codigo == correcto.codigo -> R.color.quiz_respuesta_correcta
                posicion == posicionElegida -> R.color.quiz_respuesta_incorrecta
                else -> R.color.quiz_opcion_fondo
            }

            val colorTexto = if (colorFondo == R.color.quiz_opcion_fondo) {
                R.color.quiz_opcion_texto
            } else {
                R.color.resultado_texto
            }

            boton.backgroundTintList = ColorStateList.valueOf(getColor(colorFondo))
            boton.setTextColor(getColor(colorTexto))
        }

        binding.tvRespuestaQuiz.text = if (acierto) {
            getString(R.string.quiz_acierto)
        } else {
            getString(R.string.quiz_fallo, correcto.nombre)
        }

        actualizarMarcador()
        binding.btnSiguienteQuiz.isEnabled = true
    }

    private fun actualizarMarcador() {
        binding.tvAciertosQuiz.text = getString(R.string.quiz_aciertos, aciertos)
    }

    private fun avanzar() {
        if (indicePregunta == paisesPartida.lastIndex) {
            binding.contenidoQuiz.isVisible = false
            binding.tvResultadoQuiz.text = getString(
                R.string.quiz_resultado,
                aciertos,
                TOTAL_PREGUNTAS
            )
            binding.resultadoQuiz.isVisible = true
        } else {
            indicePregunta++
            mostrarPregunta()
        }
    }

    private fun mostrarError(mensaje: Int) {
        binding.tvErrorQuiz.setText(mensaje)
        binding.tvErrorQuiz.isVisible = true
    }

    companion object {
        private const val TOTAL_PREGUNTAS = 10
    }
}