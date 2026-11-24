package com.dam.pasaporteapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dam.pasaporteapp.R
import com.dam.pasaporteapp.database.PaisVisitado
import com.dam.pasaporteapp.databinding.ItemPaisBinding
import androidx.core.view.isVisible

class PasaporteAdapter(
    private val onPaisClick: (PaisVisitado) -> Unit,
    private val onEditarNota: (PaisVisitado) -> Unit,
    private val onQuitarPais: (PaisVisitado) -> Unit
) : ListAdapter<PaisVisitado, PasaporteAdapter.VisitadoViewHolder>(DIFERENCIAS) {

    class VisitadoViewHolder(
        val binding: ItemPaisBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VisitadoViewHolder {
        val binding = ItemPaisBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VisitadoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitadoViewHolder, position: Int) {
        val pais = getItem(position)
        val binding = holder.binding

        binding.tvNombrePais.text = pais.name
        binding.tvCapital.text = pais.capital.ifBlank {
            binding.root.context.getString(R.string.no_capital)
        }
        binding.tvRegion.text = pais.region
        binding.ivVisitado.setImageResource(android.R.drawable.star_big_on)

        binding.accionesPasaporte.isVisible = true
        binding.tvNotaPasaporte.text = pais.note.ifBlank {
            binding.root.context.getString(R.string.pasaporte_sin_nota)
        }

        binding.btnEditarNota.setOnClickListener {
            onEditarNota(pais)
        }

        binding.btnQuitarPais.setOnClickListener {
            onQuitarPais(pais)
        }

        Glide.with(binding.ivBandera)
            .load(pais.flagUrl.takeIf { it.isNotBlank() })
            .placeholder(R.drawable.ic_quiz)
            .fallback(R.drawable.ic_quiz)
            .error(R.drawable.ic_quiz)
            .into(binding.ivBandera)

        binding.root.setOnClickListener {
            onPaisClick(pais)
        }
    }

    companion object {
        private val DIFERENCIAS = object : DiffUtil.ItemCallback<PaisVisitado>() {
            override fun areItemsTheSame(anterior: PaisVisitado, nuevo: PaisVisitado): Boolean {
                return anterior.code == nuevo.code
            }

            override fun areContentsTheSame(anterior: PaisVisitado, nuevo: PaisVisitado): Boolean {
                return anterior == nuevo
            }
        }
    }
}