package com.example.companyapp.company

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.companyapp.R
import com.example.companyapp.databinding.ActivityCompanyPersonalDetailsBinding
import com.example.joby.models.CompanyProfile
import com.example.joby.utils.FirebaseHelper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CompanyPersonalDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompanyPersonalDetailsBinding
    private lateinit var storageReference: StorageReference
    private lateinit var comproRef: DatabaseReference
    private var imageUri: Uri? = null
    private var companyEmail: String? = null
    private var personalKey: String? = null

    // Register for image picker result
    private val imagePickerResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                Glide.with(this@CompanyPersonalDetailsActivity)
                    .load(it)
                    .into(binding.profile)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyPersonalDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        comproRef = FirebaseHelper.companyprofileRef
        storageReference = FirebaseStorage.getInstance().reference.child("company_profile_pic")
        companyEmail = FirebaseHelper.getAuth().currentUser?.email
        binding.profileMail.setText(companyEmail)

        loadUserProfile()

        binding.changeProfilePicture.setOnClickListener {
            openImagePicker()
        }

        binding.save.setOnClickListener {
            saveProfile()
        }
    }

    // Opens image picker using the new API
    private fun openImagePicker() {
        imagePickerResult.launch("image/*")
    }

    // Save profile data (including profile picture) to Firebase
    private fun saveProfile() {
        val name = binding.profileName.text.toString()
        val website = binding.websiteLink.text.toString()
        val location = binding.profileLocation.text.toString()
        val employees = binding.employees.text.toString()

        // Check if the required fields are filled
        if (name.isEmpty() || website.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Upload profile picture if selected, then save profile data
        if (imageUri != null) {
            uploadImageToStorage { profilePicUrl ->
                saveProfileToDatabase(name, website, location, employees, profilePicUrl)
            }
        } else {
            saveProfileToDatabase(name, website, location, employees, null)
        }
    }

    // Upload image to Firebase Storage
    private fun uploadImageToStorage(onSuccess: (String) -> Unit) {
        val ref = storageReference.child("${System.currentTimeMillis()}.jpg")
        ref.putFile(imageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    // Save profile data to Firebase Realtime Database
    private fun saveProfileToDatabase(name: String, website: String, address: String, employee: String, profilePicUrl: String?) {
        companyEmail?.let { email ->
            val compro = CompanyProfile(
                company_email = email,
                company_name = name,
                company_profile_pic = profilePicUrl ?: "",
                company_location = address,
                company_employees = employee,
                company_website_link = website
            )

            // Use the existing key to update the record or create a new one if it doesn't exist
            val key = personalKey ?: comproRef.push().key ?: email.replace(".", "_")
            comproRef.child(key).setValue(compro)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Load the company profile from Firebase
    private fun loadUserProfile() {
        companyEmail?.let { email ->
            comproRef.orderByChild("company_email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                try {
                                    val personal = data.getValue(CompanyProfile::class.java)
                                    personal?.let {
                                        personalKey = data.key
                                        binding.profileName.setText(it.company_name)
                                        binding.websiteLink.setText(it.company_website_link)
                                        binding.profileLocation.setText(it.company_location)
                                        binding.employees.setText(it.company_employees)

                                        // Load profile picture using Glide
                                        Glide.with(this@CompanyPersonalDetailsActivity)
                                            .load(it.company_profile_pic)
                                            .into(binding.profile)
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        this@CompanyPersonalDetailsActivity,
                                        "Error parsing profile data: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            // No data found, clear fields if necessary
                            binding.profileName.setText("")
                            binding.websiteLink.setText("")
                            binding.profileLocation.setText("")
                            binding.employees.setText("")
                            binding.profile.setImageResource(R.drawable.profile_img) // A placeholder image
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@CompanyPersonalDetailsActivity,
                            "Error loading profile: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}
