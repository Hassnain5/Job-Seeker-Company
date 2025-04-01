//package com.example.companyapp.API
//
//// GeminiViewModelFactory.kt
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//
//class GeminiViewModelFactory(private val apiKey: String) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(GeminiViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return GeminiViewModel(apiKey) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}