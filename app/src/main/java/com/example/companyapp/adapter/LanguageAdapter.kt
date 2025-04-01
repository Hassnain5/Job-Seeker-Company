package com.example.companyapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.companyapp.R
import com.example.companyapp.databinding.RvLanguageBinding

class LanguageAdapter (private var model: ArrayList<Model>) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    class ViewHolder(val binding: RvLanguageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = model[position]
        holder.binding.apply {
            Glide.with(holder.itemView.context)
                .load(R.drawable.country_img)
                .placeholder(R.drawable.country_img)
                .error(R.drawable.star_outline)
                .into(holder.binding.flag)
            languageName.text = currentItem.text
            radioBtn.text = currentItem.text1
        }
    }

    override fun getItemCount(): Int {
        return model.size
    }
}