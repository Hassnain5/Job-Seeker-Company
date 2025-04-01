package com.example.companyapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.companyapp.R
import com.example.companyapp.models.Education


class EducationAdapter(
    private val educationList: List<Education>,
    private val onEditClick: (Education) -> Unit
) : RecyclerView.Adapter<EducationAdapter.EducationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EducationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_education, parent, false)
        return EducationViewHolder(view)
    }

    override fun onBindViewHolder(holder: EducationViewHolder, position: Int) {
        val education = educationList[position]
        holder.bind(education)
        holder.itemView.setOnClickListener {
            onEditClick(education) // Trigger edit on item click
        }
    }

    override fun getItemCount(): Int {
        return educationList.size
    }

    class EducationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val schoolTextView: TextView = itemView.findViewById(R.id.tvSchool)
        private val degreeTextView: TextView = itemView.findViewById(R.id.tvDegree)
        private val fieldTextView: TextView = itemView.findViewById(R.id.tvField)
        private val startDateTextView: TextView = itemView.findViewById(R.id.tvStartDate)
        private val endDateTextView: TextView = itemView.findViewById(R.id.tvEndDate)
        val editButton: ImageView = itemView.findViewById(R.id.btnEdit)
        val deleteButton: ImageView = itemView.findViewById(R.id.btnDelete)

        fun bind(education: Education) {
            schoolTextView.text = education.user_school
            degreeTextView.text = education.user_degree
            fieldTextView.text = education.user_field
            startDateTextView.text = education.user_start_date
            endDateTextView.text = education.user_end_date
        }
    }

}
