package com.example.fyp.ordering_system.domain.validation

data class OrderValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)