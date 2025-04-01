package com.example.companyapp.company

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.companyapp.databinding.ActivityCompanySignInBinding
import com.google.firebase.auth.FirebaseAuth

class CompanySignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompanySignInBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanySignInBinding.inflate(layoutInflater)

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            startActivity(Intent(this, CompanyNavigationActivity::class.java))
            finish() // Close the SignInActivity so it cannot be navigated back to
        }

        setContentView(binding.root)

        // Handle login button click
        binding.loginBtn.setOnClickListener {
            val email = binding.signinEmailAddress.editText?.text.toString().trim()
            val password = binding.signinPassword.editText?.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle "Do not have an account" text click
        binding.doNotHaveAccount.setOnClickListener {
            startActivity(Intent(this, CompanySignupActivity::class.java))
        }

        // Handle "Forget Password" text click
        binding.forgetPassword.setOnClickListener {
            startActivity(Intent(this, CompanyForgetPasswordActivity::class.java))
        }
    }

    // Function to log in the user
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        saveSession(email) // Save session when login is successful
                        startActivity(Intent(this, CompanyNavigationActivity::class.java))
                        finish() // Close the SignInActivity
                    } else {
                        Toast.makeText(this, "Please verify your email address.", Toast.LENGTH_SHORT).show()
                        resendVerificationEmail()
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to save the session
    private fun saveSession(email: String) {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_email", email)
            apply() // Save the email to session
        }
    }

    // Function to check if user is logged in
    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.contains("user_email") // Check if session exists
    }

    // Function to resend verification email
    private fun resendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
            if (verifyTask.isSuccessful) {
                Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}