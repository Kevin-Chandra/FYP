package com.example.fyp.pos.util

data class KitchenManageOrderItemUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val successUpdate: Boolean = false,
)