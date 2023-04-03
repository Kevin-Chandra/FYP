package com.example.fyp.menucreator.domain

import javax.inject.Inject


class ValidateProductNameUseCase{
        operator fun invoke(name: String): ProductValidationResult {
            if (name.isBlank()) {
                return ProductValidationResult(
                    successful = false,
                    errorMessage = "The name can't be blank"
                )
            }
            return ProductValidationResult(
                successful = true
            )
        }
}