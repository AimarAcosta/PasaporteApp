package com.dam.pasaporteapp.model

enum class EstiloViaje(val precioPorPersonaDia: Int) {
    MOCHILERO(50),
    ESTANDAR(100),
    LUJO(250)
}

data class PresupuestoViaje(
    val region: String,
    val dias: Int,
    val viajeros: Int,
    val estilo: EstiloViaje,
    val conSeguro: Boolean
) {
    val total: Double
        get() {
            val seguroPorDia = if (conSeguro) PRECIO_SEGURO else 0
            val precioPorPersonaDia = estilo.precioPorPersonaDia + seguroPorDia

            return dias * viajeros * precioPorPersonaDia * multiplicador(region)
        }

    companion object {
        private const val PRECIO_SEGURO = 5

        fun multiplicador(region: String): Double = when (region) {
            "Africa" -> 0.8
            "Asia" -> 0.9
            "Europe" -> 1.2
            "Oceania" -> 1.3
            else -> 1.0
        }
    }
}