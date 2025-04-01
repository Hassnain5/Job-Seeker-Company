package com.example.joby.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    val usersRef: DatabaseReference = database.getReference("users")
    val companyRef: DatabaseReference = database.getReference("company")
    val personalRef: DatabaseReference = database.getReference("Personal")
    val educationRef: DatabaseReference = database.getReference("Education")
    val experienceRef: DatabaseReference = database.getReference("Experience")
    val applicationRef: DatabaseReference = database.getReference("Application")
    val companyprofileRef:DatabaseReference= database.getReference("CompanyProfile")
    val jobpostRef:DatabaseReference= database.getReference("Jobs")


    fun getAuth(): FirebaseAuth {
        return auth
    }
}