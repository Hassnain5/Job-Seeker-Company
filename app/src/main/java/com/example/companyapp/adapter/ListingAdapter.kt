package com.example.joby.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.companyapp.R
import com.example.companyapp.databinding.RvJobListingBinding
import com.example.companyapp.viewapplicants.JobDetailActivity
import com.example.joby.models.PostJob

class ListingAdapter(private var jobList: List<PostJob>) : RecyclerView.Adapter<ListingAdapter.ViewHolder>() {

    private lateinit var context: Context

    class ViewHolder(val binding: RvJobListingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = RvJobListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentJob = jobList[position]
        holder.binding.apply {
            // Load placeholder image for the card view
            Glide.with(holder.itemView.context)
                .load(R.drawable.suggested_jobs_directory)
                .placeholder(R.drawable.suggested_jobs_directory)
                .error(R.drawable.star_outline)
                .into(holder.binding.card)

            // Bind dynamic data for job details
            Glide.with(holder.itemView.context)
                .load(R.drawable.kickstarter)
                .placeholder(R.drawable.kickstarter)
                .error(R.drawable.star_outline)
                .into(holder.binding.kickstarter)

            // Set text values based on PostJob model
            jrFrontEndDesigner.text = currentJob.job_title
            fullTimeBtn.text = currentJob.job_type
            remoteBtn.text = currentJob.job_location

            // Set click listener for the entire item
            root.setOnClickListener {
                // Create intent to open JobDetailsActivity
                val intent = Intent(context, JobDetailActivity::class.java).apply {
                    putExtra("JOB_ID", currentJob.job_id) // Assuming PostJob has an 'id' field
                    // You can add more data if needed
                    putExtra("JOB_TITLE", currentJob.job_title)
                    putExtra("JOB_TYPE", currentJob.job_type)
                    putExtra("JOB_LOCATION", currentJob.job_location)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return jobList.size
    }

    fun updateJobList(newJobList: List<PostJob>) {
        jobList = newJobList
        notifyDataSetChanged()
    }
}