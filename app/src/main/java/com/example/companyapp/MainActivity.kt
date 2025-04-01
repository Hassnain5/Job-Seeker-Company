package com.example.companyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.companyapp.company.CompanySignInActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Delay for 2 seconds before navigating to the OnboardingFragment
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, CompanySignInActivity ::class.java)
            startActivity(intent)
            finish()

        }, 2000)
    }
}