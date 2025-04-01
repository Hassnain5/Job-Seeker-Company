package com.example.companyapp.company

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.companyapp.R
import com.example.companyapp.databinding.ActivityCompanyNavigationBinding

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.companyapp.company.companyfragments.CompanyProfileFragment
import com.example.companyapp.company.companyfragments.PostNewJobFragment
import com.example.companyapp.company.companyfragments.YourListingFragment


class CompanyNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyNavigationBinding



        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityCompanyNavigationBinding.inflate(layoutInflater)
            setContentView(binding.root)


            // Set up bottom navigation view using binding
            binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        loadFragment(YourListingFragment())
                        setIcons(R.id.nav_home)
                        true
                    }
                    R.id.nav_saved -> {
                        loadFragment(PostNewJobFragment())
                        setIcons(R.id.nav_saved)
                        true
                    }
                    R.id.nav_applied -> {
                        loadFragment(CompanyProfileFragment())
                        setIcons(R.id.nav_applied)
                        true
                    }

                    else -> false
                }
            }

            // Load default fragment
            loadFragment(YourListingFragment())
            setIcons(R.id.nav_home)
        }

        private fun loadFragment(fragment: Fragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment) // Ensure fragment_container exists in the layout
                .commit()
        }

        private fun setIcons(selectedItemId: Int) {
            // Reset icons for all menu items and highlight the selected one
            binding.bottomNavigation.menu.findItem(R.id.nav_home).icon = ContextCompat.getDrawable(this, if (selectedItemId == R.id.nav_home) R.drawable.ic_home_selected else R.drawable.ic_home)
//            binding.bottomNavigation.menu.findItem(R.id.nav_saved).icon = ContextCompat.getDrawable(this, if (selectedItemId == R.id.nav_saved) R.drawable.ic_saved_selected else R.drawable.ic_saved)
            binding.bottomNavigation.menu.findItem(R.id.nav_saved).icon = ContextCompat.getDrawable(this, if (selectedItemId == R.id.nav_saved) R.drawable.ic_applied_selected else R.drawable.ic_applied_jobs)
//            binding.bottomNavigation.menu.findItem(R.id.nav_q_a).icon = ContextCompat.getDrawable(this, if (selectedItemId == R.id.nav_q_a) R.drawable.ic_qna_selected else R.drawable.ic_qna)
            binding.bottomNavigation.menu.findItem(R.id.nav_applied).icon = ContextCompat.getDrawable(this, if (selectedItemId == R.id.nav_applied) R.drawable.ic_profile_selected else R.drawable.profile)
        }
    }