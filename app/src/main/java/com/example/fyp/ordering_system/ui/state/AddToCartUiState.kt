package com.example.fyp.ordering_system.ui.state

data class AddToCartUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val successAdding: Boolean = false,
)