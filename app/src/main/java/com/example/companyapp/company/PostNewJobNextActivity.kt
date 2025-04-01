package com.example.companyapp.company

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.companyapp.R
import com.example.companyapp.databinding.ActivityPostNewJobNextBinding
import com.example.joby.models.PostJob
import com.example.joby.utils.FirebaseHelper
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.*

class PostNewJobNextActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostNewJobNextBinding
    private var selectedItem: String? = null
    private lateinit var jobRef: DatabaseReference
    private var companyEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostNewJobNextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase reference
        jobRef = FirebaseHelper.jobpostRef
        companyEmail = FirebaseHelper.getAuth().currentUser?.email

        setupSpinner()
        setupPostButton()

        // Get data passed from the previous screen
        val title = intent.getStringExtra("title")
        val descrip = intent.getStringExtra("descrip")
        val requir = intent.getStringExtra("requir")

        if (title.isNullOrEmpty() || descrip.isNullOrEmpty() || requir.isNullOrEmpty()) {
            Toast.makeText(this, "Missing required job details!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupSpinner() {
        val locationSpinner: Spinner = binding.locationSpinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.location_array,
            android.R.layout.simple_spinner_dropdown_item
        )
        locationSpinner.adapter = adapter
        locationSpinner.setSelection(0)
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedItem = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedItem = null
            }
        }
    }

    private fun setupPostButton() {
        binding.postBtn.setOnClickListener {
            val company = binding.company.text.toString()
            val jobType = binding.jobType.text.toString()
            val jobLocation = binding.jobLocation.text.toString()
            val salary = binding.jobSalary.text.toString()
            val address = binding.jobAddress.text.toString()

            val title = intent.getStringExtra("title")
            val descrip = intent.getStringExtra("descrip")
            val requir = intent.getStringExtra("requir")

            if (validateFields(title, descrip, requir,company, jobType, jobLocation, salary, address)) {
                selectedItem?.let { experience ->
                    saveJobToDatabase(
                        title!!, descrip!!, requir!!,company, jobType, jobLocation, salary, experience, address
                    )
                }
            }
        }
    }

    private fun validateFields(
        title: String?,
        descrip: String?,
        requir: String?,
        company: String,
        jobType: String,
        jobLocation: String,
        salary: String,
        address: String
    ): Boolean {
        if (title.isNullOrEmpty() || descrip.isNullOrEmpty() || requir.isNullOrEmpty() ||
            jobType.isEmpty() || jobLocation.isEmpty() || salary.isEmpty() || address.isEmpty() || selectedItem == null
        ) {
            Toast.makeText(this, "Please fill all the fields.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveJobToDatabase(
        title: String,
        descrip: String,
        requir: String,
        company: String,
        jobType: String,
        jobLocation: String,
        salary: String,
        experience: String,
        address: String
    ) {
        val date = getCurrentDate()
        val id = generateUniqueId(jobRef)

        companyEmail?.let { email ->
            val job = PostJob(
                job_id = id,
                company_email = email,
                job_title = title,
                job_description = descrip,
                job_requirment = requir,
                company = company,
                job_type = jobType,
                job_location = jobLocation,
                job_salary = salary,
                job_experience = experience,
                job_address = address,
                job_date = date
            )

            val key = jobRef.push().key ?: email.replace(".", "_")
            jobRef.child(key).setValue(job)
                .addOnSuccessListener {
                    Toast.makeText(this, "Job Posted Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to post job. Please try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun generateUniqueId(database: DatabaseReference): String {
        return database.push().key ?: UUID.randomUUID().toString()
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
