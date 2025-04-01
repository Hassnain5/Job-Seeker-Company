package com.example.companyapp.company

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.companyapp.R
import com.example.companyapp.databinding.ActivityCompanyOpenEmailBinding

class CompanyOpenEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompanyOpenEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyOpenEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val email = intent.getStringExtra("email")

        binding.openEmailApp.setOnClickListener {
            val intent = Intent(this, CompanyChangePasswordActivity::class.java)
            intent.putExtra("email",email)
            startActivity(intent)
        }

    }
}