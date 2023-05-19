package com.example.fyp.pos.util

data class ManageOrderUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val successUpdate: Boolean = false,
)