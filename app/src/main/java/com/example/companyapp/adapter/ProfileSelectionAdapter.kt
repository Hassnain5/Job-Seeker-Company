package com.example.joby.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.companyapp.databinding.ItemSelectProfileCardBinding
import com.example.companyapp.models.Application

class ProfileSelectionAdapter(
    private val profiles: List<Application>,
    private val onProfileSelected: (String) -> Unit
) : RecyclerView.Adapter<ProfileSelectionAdapter.ProfileViewHolder>() {

    inner class ProfileViewHolder(private val binding: ItemSelectProfileCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(profile: Application) {
            binding.webDeveloperTitle.text = profile.user_app_profile_name
            binding.cvDetails.text = "${profile.user_cv_url} • ${profile.user_portfolio_url}"
            binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onProfileSelected(profile.documentId)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemSelectProfileCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount(): Int = profiles.size
}