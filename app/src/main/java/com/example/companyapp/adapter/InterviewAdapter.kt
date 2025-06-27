package com.example.companyapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.companyapp.R
import com.example.companyapp.databinding.ItemInterviewBinding
import com.example.companyapp.models.Applicant
import com.example.companyapp.viewapplicants.ApplicantsViewProfileActivity
import java.text.SimpleDateFormat
import java.util.*

class InterviewAdapter : RecyclerView.Adapter<InterviewAdapter.InterviewViewHolder>() {

    private var applicants = listOf<Applicant>()

    // Debugging function
    fun submitList(newList: List<Applicant>) {
        Log.d("InterviewAdapter", "Submitting list with ${newList.size} items")
        newList.forEach {
            Log.d("InterviewAdapter", "Item: ${it.name}, ${it.email}, ${it.jobDate}")
        }
        applicants = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterviewViewHolder {
        val binding = ItemInterviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InterviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterviewViewHolder, position: Int) {
        holder.bind(applicants[position])
    }

    override fun getItemCount() = applicants.size

    inner class InterviewViewHolder(
        private val binding: ItemInterviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(applicant: Applicant) {
            try {
                binding.apply {
                    name.text = applicant.name ?: "No Name"
                    email.text = applicant.email ?: "No Email"
                    tvDescription.text =applicant.info ?: "No Description"

                    // Improved date formatting
                    val dateText = if (!applicant.jobDate.isNullOrEmpty()) {
                        try {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            val date = inputFormat.parse(applicant.jobDate) ?: Date()
                            "Interview scheduled on ${outputFormat.format(date)}"
                        } catch (e: Exception) {
                            "Interview date not specified"
                        }
                    } else {
                        "Interview date not specified"
                    }
                    dateApplied.text = dateText

                    // Load profile image
                    Glide.with(itemView.context)
                        .load(applicant.profileImageUrl)
                        .placeholder(R.drawable.profile_img)
                        .error(R.drawable.profile_img)
                        .into(profile)

                    root.setOnClickListener {
                        Log.d("InterviewAdapter", "Sending email: ${applicant.email}")
                        Intent(itemView.context, ApplicantsViewProfileActivity::class.java).apply {
                            putExtra("USER_ID", applicant.userId ?: "")
                            putExtra("USER_NAME", applicant.name ?: "")
                            putExtra("JOB_ID", applicant.jobId ?: "")

                            putExtra("USER_EMAIL", applicant.email ?: "")
                            putExtra("Application_Status", "Interview")
                            itemView.context.startActivity(this)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("InterviewAdapter", "Error binding applicant", e)
            }
        }
    }
}