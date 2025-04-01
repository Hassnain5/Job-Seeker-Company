package com.example.companyapp.API

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.companyapp.R
import com.example.companyapp.databinding.ActivityAiSuggestionBinding
import com.example.companyapp.databinding.ActivityJobDetailsBinding

class AiSuggestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiSuggestionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAiSuggestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jobId = intent.getStringExtra("JOB_ID")
        val applicantEmail = intent.getStringExtra("APPLICANT_EMAIL")
        val answers = intent.getStringExtra("ANSWERS")
Toast.makeText(this, "Job ID: $jobId", Toast.LENGTH_LONG).show()
        binding.proceedButton.setOnClickListener(){
            val intent = Intent(this, JobDetailsActivity::class.java)
            intent.putExtra("JOB_ID", jobId)
            Toast.makeText(this, "Job ID: $jobId sent sucessfully ", Toast.LENGTH_LONG).show()
            startActivity(intent)
        }

    }
}