package com.example.companyapp.adapter

import android.os.Bundle
import android.provider.Settings.Global.putString
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.companyapp.viewapplicants.ApplicantsCVFragment
import com.example.companyapp.viewapplicants.ApplicantsEducationFragment
import com.example.companyapp.viewapplicants.ApplicantsExperienceFragment

class ApplicantsViewPagerAdapter(
    activity: FragmentActivity,
    private val userId: String,
    private val jobId: String
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ApplicantsCVFragment().apply {
                arguments = Bundle().apply {
                    putString("USER_ID", userId)
                    putString("JOB_ID", jobId)
                }
            }
            1 -> ApplicantsEducationFragment().apply {
                arguments = Bundle().apply {
                    putString("USER_ID", userId)
                }
            }
            2 -> ApplicantsExperienceFragment().apply {
                arguments = Bundle().apply {
                    putString("USER_ID", userId)
                }
            }
            else -> ApplicantsCVFragment() // Fallback, though getItemCount limits position to 0-2
        }
    }
}