package com.example.companyapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.companyapp.R
import com.example.companyapp.databinding.ItemApplicantsBinding
import com.example.companyapp.models.Applicant
import com.example.companyapp.viewapplicants.ApplicantsViewProfileActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApplicationsAdapter(
    private  var context: Context

) : RecyclerView.Adapter<ApplicationsAdapter.ApplicantViewHolder>(

) {

    private var applicants = listOf<Applicant>()

    fun submitList(newList: List<Applicant>) {
        applicants = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        context = parent.context
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
            binding.dateApplied.text = "Applied on ${applicant.jobDate.substringBefore(" ")}"

            binding.tvDescription.text =applicant.info ?: "No Description"

            // In ApplicationsAdapter:
            val dateText = if (!applicant.jobDate.isNullOrEmpty()) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val date = inputFormat.parse(applicant.jobDate) ?: Date()
                    "Applied on ${SimpleDateFormat("MMM dd, yyyy").format(date)}"
                } catch (e: Exception) {
                    applicant.jobDate.substringBefore(" ")
                }
            } else {
                "Applied on Unknown Date"
            }
            // Load profile image
            if (!applicant.profileImageUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(applicant.profileImageUrl)
                    .placeholder(R.drawable.profile_img)
                    .into(binding.profile)
            } else {
                binding.profile.setImageResource(R.drawable.profile_img)
            }

            // In ApplicationsAdapter's bind function:
            binding.root.setOnClickListener {
                val intent = Intent(context, ApplicantsViewProfileActivity::class.java).apply {
                    putExtra("USER_ID", applicant.userId ?: "") // Use userId not email
                    putExtra("JOB_ID", applicant.jobId ?: "")
                    putExtra("USER_NAME", applicant.name ?: "Unknown")
                    putExtra("USER_EMAIL", applicant.email ?: "") // Add this line
                    putExtra("Application_Status", "Applied")
                }
                context.startActivity(intent)
            }
        }
    }
}