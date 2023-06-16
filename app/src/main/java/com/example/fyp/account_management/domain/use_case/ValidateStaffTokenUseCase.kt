package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.util.Response
import javax.inject.Inject

class ValidateStaffTokenUseCase @Inject constructor(
    private val getRegisterStaffTokenUseCase: GetRegisterStaffTokenUseCase
) {

    suspend operator fun invoke(token: String): ValidationResult {
        return when (val response = getRegisterStaffTokenUseCase()) {
            is Response.Error -> {
                ValidationResult(
                    successful = false,
                    errorMessage = response.exception.message
                )
            }
            Response.Loading -> {
                ValidationResult(
                    successful = false
                )
            }
            is Response.Success -> {
                if (token != response.data) {
                    ValidationResult(
                        successful = false,
                        errorMessage = "Invalid Token!"
                    )
                } else {
                    ValidationResult(
                        successful = true
                    )
                }
            }

        }
    }
}