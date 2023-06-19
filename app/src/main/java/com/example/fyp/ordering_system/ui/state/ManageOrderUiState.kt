package com.example.fyp.ordering_system.ui.state

data class IncomingOrderUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val successUpdate: Boolean = false,
//    val data: List<Order> = emptyList(),
)