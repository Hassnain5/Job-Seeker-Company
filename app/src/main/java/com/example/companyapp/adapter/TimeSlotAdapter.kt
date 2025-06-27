package com.example.companyapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.companyapp.R
import com.example.companyapp.models.TimeSlot
class TimeSlotAdapter(
    private val timeSlots: MutableList<TimeSlot>,
    private val onRemoveClickListener: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val btnRemove: TextView = itemView.findViewById(R.id.btn_remove_slot)

        fun bind(timeSlot: TimeSlot) {
            tvDate.text = timeSlot.date
            tvTime.text = timeSlot.time
            tvDay.text = timeSlot.dayOfWeek

            btnRemove.setOnClickListener {
                onRemoveClickListener(timeSlot)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }

    override fun getItemCount(): Int = timeSlots.size

    fun updateList(newList: List<TimeSlot>) {
        timeSlots.clear()
        timeSlots.addAll(newList)
        notifyDataSetChanged() // This is crucial for updates
    }
}