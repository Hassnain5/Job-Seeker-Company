package com.example.companyapp.company

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.companyapp.R
import com.example.companyapp.databinding.ActivityCompanyChangePasswordBinding
import com.example.joby.utils.FirebaseHelper

class CompanyChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompanyChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val email = intent.getStringExtra("email")
        binding.changePasswordBtn.setOnClickListener {
            val newpass=binding.etNewpass.text.toString().trim()
            val confpass=binding.etConfpass.text.toString().trim()
            if (newpass == confpass){
                if (email != null) {
                    updatePasswordInDatabase(newpass,email)
                }
            }
            else{
                Toast.makeText(this,"Enter Same Password", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updatePasswordInDatabase(newPassword: String,email:String) {

        FirebaseHelper.companyRef.orderByChild("company_email").equalTo(email).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userSnapshot = snapshot.children.first()
                    val userId = userSnapshot.key

                    // Update the password in the Realtime Database
                    userId?.let { id ->
                        FirebaseHelper.usersRef.child(id).child("company_password")
                            .setValue(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@CompanyChangePasswordActivity,
                                        "Password updated in the database",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this, CompanySignInActivity::class.java))
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to update password in the database",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "User not found in database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }
}