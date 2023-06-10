package com.example.fyp.menucreator.domain.validation

import com.example.fyp.menucreator.domain.ProductValidationResult

class ValidateModifierSelectionRangeUseCase{
        operator fun invoke(
            required: Boolean,
            multipleChoice: Boolean,
            minItem: Int = 0,
            listSize: Int
        ): ProductValidationResult {
            if (required){
                if (listSize < 1){
                    return ProductValidationResult(
                        successful = false,
                        errorMessage = "Required modifier must have at least one item!"
                    )
                }
                if (multipleChoice){
                    if (minItem < 1)
                        return ProductValidationResult(
                            successful = false,
                            errorMessage = "Required modifier minimum selection must be at least one item!"
                        )
                }
            } else {
                if (listSize < 1){
                    return ProductValidationResult(
                        successful = false,
                        errorMessage = "Modifier must have at least one item!"
                    )
                }
                if (multipleChoice && minItem != 0)
                    return ProductValidationResult(
                        successful = false,
                        errorMessage = "Optional modifier minimum selection must be zero!"
                    )
            }
            return ProductValidationResult(
                successful = true
            )
        }
}