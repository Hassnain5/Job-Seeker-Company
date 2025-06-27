package com.example.companyapp.models

data class Applicant(
    val jobId: String,
    val email: String,
    val userId: String,  // Add this field to store user ID
    val name: String,    // Add name field
    val info: String,
    val cvUrl: String,
    val portfolioUrl: String,
    val jobName: String,
    val jobDate: String,
    val profileImageUrl: String? = null  // Add profile image URL
)
