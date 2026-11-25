package com.kevcruz.parqueoslapaz.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kevcruz.parqueoslapaz.R
import com.kevcruz.parqueoslapaz.data.models.Parqueo

class BusquedaAdapter(
    private val onItemClick: (Parqueo) -> Unit
) : RecyclerView.Adapter<BusquedaAdapter.ViewHolder>() {

    private var parqueos: List<Parqueo> = emptyList()

    fun setParqueos(nuevosParqueos: List<Parqueo>) {
        parqueos = nuevosParqueos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parqueo_busqueda, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(parqueos[position])
    }

    override fun getItemCount() = parqueos.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreParqueo)
        private val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccion)
        private val tvTarifa: TextView = itemView.findViewById(R.id.tvTarifa)
        private val tvDisponibilidad: TextView = itemView.findViewById(R.id.tvDisponibilidad)

        fun bind(parqueo: Parqueo) {
            tvNombre.text = parqueo.nombre
            tvDireccion.text = parqueo.direccion
            tvTarifa.text = "Bs ${parqueo.tarifa_por_hora}/hora"

            val textoDisponibilidad = if (parqueo.cupos_libres > 0) {
                "${parqueo.cupos_libres} libres"
            } else {
                "Lleno"
            }
            tvDisponibilidad.text = textoDisponibilidad
            tvDisponibilidad.setTextColor(
                if (parqueo.cupos_libres > 0)
                    itemView.context.getColor(android.R.color.holo_green_dark)
                else
                    itemView.context.getColor(android.R.color.holo_red_dark)
            )

            itemView.setOnClickListener { onItemClick(parqueo) }
        }
    }
}