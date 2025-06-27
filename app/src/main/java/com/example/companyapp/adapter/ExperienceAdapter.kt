package com.example.companyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.companyapp.databinding.ItemExperienceBinding
import com.example.companyapp.models.Experience

class ExperienceAdapter(
    private val experienceList: List<Experience> ,
    private val onItemClick: (Experience) -> Unit// Changed to use Experience data class
) : RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder>() {

    inner class ExperienceViewHolder(private val binding: ItemExperienceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(experience: Experience) {
            binding.apply {
                tvCompanyName.text = experience.user_company_name ?: "Not specified"
                tvJobTitle.text = experience.user_job_title ?: "Not specified"
                tvEmploymentType.text = experience.user_emp_type ?: "Not specified"
                tvStartDate.text = experience.user_job_start_date ?: "Not specified"
                tvEndDate.text = experience.user_job_end_date ?: "Present" // Common for current jobs

                // You can add more complex formatting if needed
                // Example: durationTextView.text = "${startDate} - ${endDate}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
        val binding = ItemExperienceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExperienceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
        holder.bind(experienceList[position])
    }

    override fun getItemCount(): Int = experienceList.size
}