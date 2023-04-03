package com.example.fyp.menucreator.domain


class ValidateProductPriceUseCase {
        operator fun invoke(price: String): ProductValidationResult {
            if (price.isBlank())
                return ProductValidationResult(
                    successful = false,
                    errorMessage = "Price can't be blank"
                )
            if (price.toDouble() < 0.0)
                return ProductValidationResult(
                    successful = false,
                    errorMessage = "Price cannot be negative!"
                )
            return ProductValidationResult(
                successful = true
            )
        }
}