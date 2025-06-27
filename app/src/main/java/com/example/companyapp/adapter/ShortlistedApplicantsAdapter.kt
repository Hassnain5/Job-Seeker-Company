package com.example.companyapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.companyapp.R
import com.example.companyapp.databinding.ItemShortlistedBinding
import com.example.companyapp.models.Applicant
import com.example.companyapp.viewapplicants.ApplicantsViewProfileActivity
import java.text.SimpleDateFormat
import java.util.*

class ShortlistedApplicantsAdapter(
    private val context: Context
) : RecyclerView.Adapter<ShortlistedApplicantsAdapter.ShortlistedViewHolder>() {

    private var applicants = listOf<Applicant>()
    var onViewProfileClick: ((Applicant) -> Unit)? = null
    var onContactClick: ((Applicant) -> Unit)? = null

    fun submitList(newList: List<Applicant>) {
        Log.d("ShortlistedAdapter", "Submitting list with ${newList.size} items")
        applicants = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortlistedViewHolder {
        val binding = ItemShortlistedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShortlistedViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ShortlistedViewHolder, position: Int) {
        holder.bind(applicants[position])
    }

    override fun getItemCount() = applicants.size

    inner class ShortlistedViewHolder(
        private val binding: ItemShortlistedBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(applicant: Applicant) {
            Log.d("ShortlistedAdapter", "Binding applicant: ${applicant.name}, email: ${applicant.email}, jobDate: ${applicant.jobDate}")

            binding.apply {
                name.text = applicant.name ?: "No Name"
                email.text = applicant.email ?: "No Email"

                tvDescription.text =applicant.info ?: "No Description"

                // Format the application date
                val dateText = if (!applicant.jobDate.isNullOrEmpty()) {
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val date = inputFormat.parse(applicant.jobDate) ?: Date()
                        "Shortlisted on ${outputFormat.format(date)}"
                    } catch (e: Exception) {
                        Log.e("ShortlistedAdapter", "Error parsing jobDate: ${applicant.jobDate}, error: ${e.message}")
                        "Shortlisted on Unknown Date"
                    }
                } else {
                    "Shortlisted on Unknown Date"
                }
                dateApplied.text = dateText

                // Load profile image
                if (!applicant.profileImageUrl.isNullOrEmpty()) {
                    Glide.with(root.context)
                        .load(applicant.profileImageUrl)
                        .placeholder(R.drawable.profile_img)
                        .into(profile)
                } else {
                    profile.setImageResource(R.drawable.profile_img)
                }

                binding.root.setOnClickListener {
                    applicant.email?.let { email ->
                        Log.d("ShortlistedAdapter", "Sending userId: ${applicant.userId}, email: $email")
                        val intent = Intent(context, ApplicantsViewProfileActivity::class.java).apply {
                            putExtra("USER_ID", applicant.userId ?: "")
                            putExtra("JOB_ID", applicant.jobId ?: "")
                            putExtra("USER_NAME", applicant.name ?: "Unknown")
                            putExtra("USER_EMAIL", email) // Send original email
                            putExtra("Application_Status", "Shortlisted")
                        }
                        context.startActivity(intent)
                    } ?: run {
                        Toast.makeText(context, "No email available", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}