package com.example.companyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.companyapp.R
import com.example.companyapp.databinding.ItemApplicantsBinding
import com.example.companyapp.models.Applicant

class ApplicationsAdapter : RecyclerView.Adapter<ApplicationsAdapter.ApplicantViewHolder>() {

    private var applicants = listOf<Applicant>()

    fun submitList(newList: List<Applicant>) {
        applicants = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val binding = ItemApplicantsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ApplicantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {
        holder.bind(applicants[position])
    }

    override fun getItemCount() = applicants.size

    inner class ApplicantViewHolder(private val binding: ItemApplicantsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(applicant: Applicant) {
            binding.name.text = applicant.name
            binding.email.text = applicant.email
            binding.jobApplied.text = "Applied on ${applicant.jobDate.substringBefore(" ")}"

            // Load profile image
            if (!applicant.profileImageUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(applicant.profileImageUrl)
                    .placeholder(R.drawable.profile_img)
                    .into(binding.profile)
            } else {
                binding.profile.setImageResource(R.drawable.profile_img)
            }

            binding.root.setOnClickListener {
                // Handle click
            }
        }
    }
}