package com.example.companyapp.viewapplicants

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.companyapp.R
import com.example.companyapp.adapter.ViewPagerAdapter
import com.example.companyapp.databinding.ActivityJobDetailBinding
import com.google.android.material.tabs.TabLayoutMediator
import android.content.Intent
import android.widget.Toast
import com.example.companyapp.API.AiSuggestionActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class JobDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get job data from intent
        val jobId = intent.getStringExtra("JOB_ID") ?: ""
        val jobTitle = intent.getStringExtra("JOB_TITLE") ?: ""

        // Set job title using View Binding
        binding.tvJobTitle.text = "Applications for $jobTitle"

        // Set up ViewPager2 with fragments
        setupViewPager()

        // Set up Floating Action Button
        setupFloatingActionButton()
    }

    private fun setupViewPager() {
        val jobId = intent.getStringExtra("JOB_ID") ?: ""

        val adapter = ViewPagerAdapter(this)

        // Create fragments with arguments
        val applicationsFragment = ApplicationsFragment().apply {
            arguments = Bundle().apply {
                putString("JOB_ID", jobId)
            }
        }

        val shortlistedFragment = ShortlistedFragment().apply {
            arguments = Bundle().apply {
                putString("JOB_ID", jobId)
            }
        }

        val interviewFragment = InterviewFragment().apply {
            arguments = Bundle().apply {
                putString("JOB_ID", jobId)
            }
        }

        adapter.addFragment(applicationsFragment, "Applications")
        adapter.addFragment(shortlistedFragment, "Shortlisted")
        adapter.addFragment(interviewFragment, "Interview")

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }

    private fun setupFloatingActionButton() {
        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener {
            // Handle FAB click
            // For example, you might want to:
            // 1. Add a new applicant
            // 2. Share job details
            // 3. Edit job posting
            // Here's an example of starting a new activity:


            val intent = Intent(this, AiSuggestionActivity::class.java).apply {
                putExtra("JOB_ID", intent.getStringExtra("JOB_ID"))
                putExtra("JOB_TITLE", intent.getStringExtra("JOB_TITLE"))
            }
            startActivity(intent)


            // For now, just show a simple toast
            Toast.makeText(this, "FAB Clicked", Toast.LENGTH_SHORT).show()
        }

        // You can customize the FAB icon programmatically if needed
        // fab.setImageResource(R.drawable.ic_add)
    }
}