package com.example.fyp.account_management.domain.use_case

class ValidateNameUseCase {

    operator fun invoke(name: String): ValidationResult {
        if(name.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "The name can't be blank"
            )
        }
        if(name.contains("\\d".toRegex())) {
            return ValidationResult(
                successful = false,
                errorMessage = "Name can't contain number"
            )
        }
        return ValidationResult(
            successful = true
        )
    }
}