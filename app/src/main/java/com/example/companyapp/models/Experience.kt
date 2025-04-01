package com.example.companyapp.models

data class Experience(
    var user_email: String? = null,
    var user_job_title: String? = null,
    var user_emp_type: String? = null,
    var user_company_name: String? = null,
    var user_job_start_date: String? = null,
    var user_job_end_date: String? = null,
    var documentId: String? = null
)
