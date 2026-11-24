package com.dam.pasaporteapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dam.pasaporteapp.databinding.ItemPaisBinding
import com.dam.pasaporteapp.model.Pais
import com.bumptech.glide.Glide
import com.dam.pasaporteapp.R

class PaisAdapter(
    private val onPaisClick: (Pais) -> Unit
) : RecyclerView.Adapter<PaisAdapter.PaisViewHolder>() {

    private var listaPaises: List<Pais> = emptyList()

    fun actualizarPaises(paises: List<Pais>) {
        listaPaises = paises
        notifyDataSetChanged()
    }

    inner class PaisViewHolder(
        private val binding: ItemPaisBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun mostrar(pais: Pais) {
            binding.tvNombrePais.text = pais.nombre
            binding.tvCapital.text = pais.capital
                ?.takeIf { it.isNotBlank() }
                ?: binding.root.context.getString(R.string.no_capital)
            binding.tvRegion.text = pais.region.orEmpty()

            Glide.with(binding.ivBandera)
                .load(pais.urlBandera)
                .placeholder(R.drawable.ic_quiz)
                .fallback(R.drawable.ic_quiz)
                .error(R.drawable.ic_quiz)
                .into(binding.ivBandera)

            binding.root.setOnClickListener {
                onPaisClick(pais)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaisViewHolder {
        val binding = ItemPaisBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PaisViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PaisViewHolder,
        position: Int
    ) {
        val pais = listaPaises[position]
        holder.mostrar(pais)
    }

    override fun getItemCount(): Int {
        return listaPaises.size
    }
}