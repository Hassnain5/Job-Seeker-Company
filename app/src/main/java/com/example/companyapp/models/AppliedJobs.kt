package com.example.joby.models

data class AppliedJobs(
    val job_id: String = "",
    val job_name: String = "",
    val user_email: String = "",
    var cv_url: String = "",
    var portfolio_url: String = "",
    var info: String = "",
    val job_status:String="",
    val job_date:String=""
)