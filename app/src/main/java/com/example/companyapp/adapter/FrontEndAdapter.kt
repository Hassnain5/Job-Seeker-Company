package com.example.companyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.companyapp.R
import com.example.companyapp.databinding.RvFrontEndJobsBinding

class FrontEndAdapter(private var recipeModel: ArrayList<RecipeModel>) : RecyclerView.Adapter<FrontEndAdapter.ViewHolder>() {

    class ViewHolder(val binding: RvFrontEndJobsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvFrontEndJobsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = recipeModel[position]
        holder.binding.apply {
            Glide.with(holder.itemView.context)
                .load(R.drawable.suggested_jobs_directory)
                .placeholder(R.drawable.suggested_jobs_directory)
                .error(R.drawable.star_outline)
                .into(holder.binding.card)
            Glide.with(holder.itemView.context)
                .load(R.drawable.kickstarter)
                .placeholder(R.drawable.kickstarter)
                .error(R.drawable.star_outline)
                .into(holder.binding.kickstarter)
            Glide.with(holder.itemView.context)
                .load(R.drawable.star_outline)
                .placeholder(R.drawable.star_outline)
                .error(R.drawable.star)
                .into(holder.binding.starOutline)
            jrFrontEndDesigner.text = currentItem.text
            kickstarterInManchester.text = currentItem.text1
            fullTimeBtn.text = currentItem.text2
            remoteBtn.text = currentItem.text3
            posted6HoursAgo.text = currentItem.text4
        }
    }

    override fun getItemCount(): Int {
        return recipeModel.size
    }
}
