package com.example.fyp.ordering_system.domain.validation

class ValidateQuantityUseCase {
    operator fun invoke(quantity: Int): OrderValidationResult {
        if (quantity <= 0)
            return OrderValidationResult(
                successful = false,
                errorMessage = "Quantity must be at least one!"
            )
        return OrderValidationResult(
            successful = true
        )
    }
}