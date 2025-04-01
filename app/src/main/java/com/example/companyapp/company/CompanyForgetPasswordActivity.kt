package com.example.companyapp.company

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.companyapp.R
import com.example.companyapp.databinding.ActivityCompanyForgetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class CompanyForgetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompanyForgetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCompanyForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.resetPassword.setOnClickListener {
            val email = binding.etemail.text.toString().trim()

            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
                val intent = Intent(this, CompanyOpenEmailActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backToLogin.setOnClickListener {
            startActivity(Intent(this, CompanySignInActivity::class.java))
        }

    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()


                } else {
                    Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                }
            }
    }
}