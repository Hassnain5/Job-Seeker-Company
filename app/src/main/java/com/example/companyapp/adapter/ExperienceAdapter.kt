package com.example.companyapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.companyapp.databinding.ItemExperienceBinding
import com.example.companyapp.models.Experience


class ExperienceAdapter(
    private val experienceList: List<Experience>,
    private val onItemClick: (Experience) -> Unit
) : RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder>() {

    inner class ExperienceViewHolder(val binding: ItemExperienceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
        val binding = ItemExperienceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExperienceViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
        if (position >= experienceList.size) {
            Log.e("ExperienceAdapter", "Position $position is out of bounds for experienceList of size ${experienceList.size}")
            return
        }

        val experience = experienceList[position]
        holder.binding.apply {
            tvJobTitle.text = experience.user_job_title
            tvEmploymentType.text = experience.user_emp_type
            tvCompanyName.text = experience.user_company_name
            tvStartDate.text = experience.user_job_start_date
            tvEndDate.text = experience.user_job_end_date

            root.setOnClickListener {
                onItemClick(experience)
            }
        }
    }

    override fun getItemCount(): Int = experienceList.size
}
