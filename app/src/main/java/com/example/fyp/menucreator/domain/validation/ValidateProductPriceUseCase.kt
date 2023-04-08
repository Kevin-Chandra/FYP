package com.example.fyp.menucreator.domain.validation

import com.example.fyp.menucreator.domain.ProductValidationResult


class ValidateProductPriceUseCase {
        operator fun invoke(price: String): ProductValidationResult {
            try {
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
            }catch (e: NumberFormatException){
                return ProductValidationResult(
                    successful = false,
                    errorMessage = "Price format is invalid!"
                )
            }catch (e: Exception){
                return ProductValidationResult(
                    successful = false,
                    errorMessage = "Unknown Error"
                )
            }
        }
}