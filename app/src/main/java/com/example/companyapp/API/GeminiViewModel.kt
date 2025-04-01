//package com.example.companyapp.API
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.google.ai.client.generativeai.GenerativeModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import androidx.lifecycle.viewModelScope
//import com.example.companyapp.API.Interfaces.GeminiApiService
//
//
//
//class GeminiViewModel(apiKey: String) : ViewModel() {
//    private val apiService = GeminiApiService(apiKey)
//
//    private val _response = MutableStateFlow<String?>(null)
//    val response: StateFlow<String?> = _response
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    private val _error = MutableStateFlow<String?>(null)
//    val error: StateFlow<String?> = _error
//
//    fun sendToGemini(userInput: String) {
//        _isLoading.value = true
//        _error.value = null
//
//        viewModelScope.launch {
//            when (val result = apiService.generateText(userInput)) {
//                is ApiResult.Success -> {
//                    _response.value = result.data
//                }
//                is ApiResult.Error -> {
//                    _error.value = "Error: ${result.exception.localizedMessage}"
//                }
//            }
//            _isLoading.value = false
//        }
//    }
//}