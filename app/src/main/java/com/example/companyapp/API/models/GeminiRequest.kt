//package com.example.companyapp.API.models
//
//// For future extensibility (if sending structured requests)
//data class GeminiRequest(
//    val contents: List<Content>,
//    val generationConfig: GenerationConfig? = null,
//    val safetySettings: List<SafetySetting>? = null
//)
//
//data class Content(
//    val parts: List<Part>,
//    val role: String = "user" // "user" or "model"
//)
//
//data class Part(
//    val text: String
//)
//
//data class GenerationConfig(
//    val temperature: Double = 0.9,
//    val topK: Int = 1,
//    val topP: Double = 1.0,
//    val maxOutputTokens: Int = 2048
//)
//
//data class SafetySetting(
//    val category: String, // e.g., "HARM_CATEGORY_DANGEROUS_CONTENT"
//    val threshold: String // e.g., "BLOCK_ONLY_HIGH"
//)
//
//// Response model (simplified)
//data class GeminiResponse(
//    val candidates: List<Candidate>
//)
//
//data class Candidate(
//    val content: Content,
//    val finishReason: String,
//    val index: Int,
//    val safetyRatings: List<SafetyRating>
//)
//
//data class SafetyRating(
//    val category: String,
//    val probability: String
//)