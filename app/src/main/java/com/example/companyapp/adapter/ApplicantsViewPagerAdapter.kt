package com.example.companyapp.adapter


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.companyapp.viewapplicants.ApplicantsCVFragment
import com.example.companyapp.viewapplicants.ApplicantsEducationFragment
import com.example.companyapp.viewapplicants.ApplicantsExperienceFragment

class ApplicantsViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ApplicantsCVFragment()
            1 -> ApplicantsEducationFragment()
            2 -> ApplicantsExperienceFragment()
            else -> ApplicantsCVFragment()
        }
    }
}
