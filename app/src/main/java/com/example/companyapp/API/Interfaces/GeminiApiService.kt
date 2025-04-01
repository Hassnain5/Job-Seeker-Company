//package com.example.companyapp.API.Interfaces
//
//import com.example.companyapp.API.ApiResult
//import com.google.ai.client.generativeai.GenerativeModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//// GeminiApiService.kt
//
//import kotlinx.coroutines.withContext
//
//class GeminiApiService(private val apiKey: String) {
//    private val generativeModel by lazy {
//        GenerativeModel(
//            modelName = "gemini-1.0-pro",
//            apiKey = apiKey
//        )
//    }
//
//    suspend fun generateText(prompt: String): ApiResult<String> {
//        return try {
//            withContext(Dispatchers.IO) {
//                val response = generativeModel.generateContent(prompt)
//                ApiResult.Success(response.text ?: "No response generated")
//            }
//        } catch (e: Exception) {
//            ApiResult.Error(e)
//        }
//    }
//}