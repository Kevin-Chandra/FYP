package com.example.fyp.account_management.ui

sealed class AuthState<out T> {

    object Loading: AuthState<Nothing>()

    data class Success<out T>( val data : T) : AuthState<T>()

    data class Failure(val error: Exception?) : AuthState<Nothing>()
}