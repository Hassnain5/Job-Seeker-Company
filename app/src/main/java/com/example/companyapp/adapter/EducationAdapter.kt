package com.example.companyapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.companyapp.R
import com.example.companyapp.models.Education

class EducationAdapter(
    private var educationList: List<Education>
) : RecyclerView.Adapter<EducationAdapter.EducationViewHolder>() {

    class EducationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val schoolTv: TextView = itemView.findViewById(R.id.tvSchool)
        private val degreeTv: TextView = itemView.findViewById(R.id.tvDegree)
        private val fieldTv: TextView = itemView.findViewById(R.id.tvField)
        private val datesTv: TextView = itemView.findViewById(R.id.tvDates)

        fun bind(education: Education) {
            schoolTv.text = education.user_school
            degreeTv.text = education.user_degree
            fieldTv.text = education.user_field
            datesTv.text = "${education.user_start_date} - ${education.user_end_date}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EducationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_education, parent, false)
        return EducationViewHolder(view)
    }

    override fun onBindViewHolder(holder: EducationViewHolder, position: Int) {
        holder.bind(educationList[position])
    }

    override fun getItemCount() = educationList.size

    fun updateList(newEducations: List<Education>) {
        this.educationList = newEducations
        notifyDataSetChanged() // This is crucial!
}}