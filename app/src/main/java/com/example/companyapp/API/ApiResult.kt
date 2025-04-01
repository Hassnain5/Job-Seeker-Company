//package com.example.companyapp.API
//
//// ApiResult.kt
//sealed class ApiResult<out T> {
//    data class Success<out T>(val data: T) : ApiResult<T>()
//    data class Error(val exception: Throwable) : ApiResult<Nothing>()
//}