package com.example.fyp.menucreator.domain.validation

import com.example.fyp.menucreator.domain.ProductValidationResult


class ValidateProductCategoryUseCase {
        operator fun invoke(category: String): ProductValidationResult {
            if (category.isBlank()) {
                return ProductValidationResult(
                    successful = false,
                    errorMessage = "Please select product category"
                )
            }
            return ProductValidationResult(
                successful = true
            )
        }
}