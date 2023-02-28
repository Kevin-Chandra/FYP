package com.example.fyp.account_management.domain.use_case

import android.util.Patterns

class ValidatePhoneUseCase {

    operator fun invoke(phone: String): ValidationResult {
        if(phone.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Phone number can't be blank"
            )
        }
        if(!Patterns.PHONE.matcher(phone).matches()) {
            return ValidationResult(
                successful = false,
                errorMessage = "That's not a valid phone number"
            )
        }
        return ValidationResult(
            successful = true
        )
    }
}