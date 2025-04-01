package com.example.joby.models

data class PostJob(
    val job_id:String="",
    val company_email:String="",
    val job_title: String = "",
    val job_description: String = "",
    val job_requirment: String = "",
    val company: String ="",
    val job_type: String = "",
    val job_location: String = "",
    val job_salary: String = "",
    val job_experience: String = "",
    val job_address:String="",
    val job_date:String=""
)
