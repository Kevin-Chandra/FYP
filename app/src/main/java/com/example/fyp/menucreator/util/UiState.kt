package com.example.fyp.menucreator.util

sealed class UiState<out T> {
    object Loading: UiState<Nothing>()

    data class Success<out T>(
        val data: T
    ): UiState<T>()

    data class Failure(
        val e: Exception?
    ): UiState<Nothing>()
}