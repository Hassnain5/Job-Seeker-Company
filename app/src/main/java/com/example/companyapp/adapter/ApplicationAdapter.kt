package com.example.companyapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.companyapp.R
import com.example.companyapp.models.Application

class ApplicationAdapter(
    private val applications: List<Application>,
    private val onItemClick: (Application?) -> Unit
) : RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val application = applications[position]
        holder.bind(application)
        holder.itemView.setOnClickListener {
            onItemClick(application)
        }
    }

    override fun getItemCount(): Int = applications.size

    class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileName: TextView = itemView.findViewById(R.id.tvProfileName)

        fun bind(application: Application) {
            profileName.text = application.user_app_profile_name
        }
    }
}
