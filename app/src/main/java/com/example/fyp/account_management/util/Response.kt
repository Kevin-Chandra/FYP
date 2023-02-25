package com.example.fyp.account_management.util

import com.example.fyp.account_management.data.Result

sealed class Response<out T> {
    data class Success<out T : Any>(val data: T) : Response<T>()
    data class Error(val exception: Exception) : Response<Nothing>()
    object Loading : Response<Nothing>()
}