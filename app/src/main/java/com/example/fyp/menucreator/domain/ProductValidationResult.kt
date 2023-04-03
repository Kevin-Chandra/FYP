package com.example.fyp.menucreator.domain

data class ProductValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null
)