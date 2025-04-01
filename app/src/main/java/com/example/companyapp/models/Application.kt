package com.example.companyapp.models

data class Application(
    val user_email: String = "",
    val user_app_profile_name: String = "",
    var user_cv_url: String = "",
    var user_portfolio_url: String = "",
    var documentId: String = ""
)
