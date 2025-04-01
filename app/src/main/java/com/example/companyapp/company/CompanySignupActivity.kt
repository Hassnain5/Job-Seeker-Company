package com.example.companyapp.company

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.companyapp.R
import com.example.companyapp.company.companyfragments.YourListingFragment
import com.example.companyapp.databinding.ActivityCompanySignupBinding
import com.example.joby.models.Company
import com.example.joby.utils.FirebaseHelper
import com.example.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.FirebaseDatabase

class CompanySignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompanySignupBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle create account button click
        binding.createAccount.setOnClickListener {
            val email = binding.tvEmailAddress.editText?.text.toString().trim()
            val password = binding.tvPassword.editText?.text.toString().trim()
            val username = binding.tvFullName.editText?.text.toString().trim()
            val confPass = binding.tvConfirmPassword.editText?.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                if (password == confPass) {
                    registerUser(email, password, username)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle "Already have an account" click
        binding.iAlreadyHaveAnAccount.setOnClickListener {
            startActivity(Intent(this, CompanySignInActivity::class.java))
        }
    }

    // Function to register the user with email and password
    private fun registerUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show()
                            checkEmailVerification(username, email, password)
                        } else {
                            Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    handleRegistrationFailure(task.exception)
                }
            }
    }

    // Function to handle registration errors
    private fun handleRegistrationFailure(exception: Exception?) {
        if (exception is FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Registration failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to check email verification and proceed with database registration if verified
    private fun checkEmailVerification(username: String, email: String, password: String) {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful && user.isEmailVerified) {
                registerInDatabase(username, email, password)
            } else {
                Toast.makeText(this, "Please verify your email address before proceeding.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to register the user in Firebase Realtime Database
    private fun registerInDatabase(username: String, email: String, password: String) {
        val userId = auth.currentUser?.uid
        val user = User(
            user_name = username,
            user_email = email,
            user_password = password
        )

        userId?.let {
            database.child(it).setValue(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, YourListingFragment::class.java))
                    finish() // Close SignUpActivity
                }
                .addOnFailureListener { excep ->
                    Toast.makeText(this, "Database error: ${excep.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}