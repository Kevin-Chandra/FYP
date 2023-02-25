package com.example.fyp.account_management.domain.use_case

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)
