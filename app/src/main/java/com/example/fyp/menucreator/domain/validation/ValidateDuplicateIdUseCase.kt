package com.example.fyp.menucreator.domain.validation

import com.example.fyp.menucreator.domain.ProductValidationResult


class ValidateDuplicateIdUseCase{
        operator fun invoke(list:List<String>): ProductValidationResult {
            if ( list.groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
                .any { it.value > 1 } ) {
                return ProductValidationResult(
                    successful = false,
                    errorMessage = "Duplicate Item ID found!"
                )
            }
            return ProductValidationResult(
                successful = true
            )
        }
}