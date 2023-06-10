package com.example.fyp.ordering_system.domain.validation

import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem

class ValidateModifierUseCase {
    operator fun invoke(modifier: Modifier, list: List<ModifierItem>?): OrderValidationResult {
        if (modifier.required) {
            if (list.isNullOrEmpty()) {
                return OrderValidationResult(
                    successful = false,
                    errorMessage = "Please select at least 1 item!"
                )
            }
            if (modifier.multipleChoice) {
                if (modifier.minItem!! > list.size)
                    return OrderValidationResult(
                        successful = false,
                        errorMessage = "Please select ${modifier.minItem} items"
                    )
                if (list.size > modifier.maxItem!!)
                    return OrderValidationResult(
                        successful = false,
                        errorMessage = "Please select maximum ${modifier.maxItem} modifier item for ${modifier.name}!"
                    )
            }
        } else {
            if (!list.isNullOrEmpty() && modifier.multipleChoice){
                if (list.size > modifier.maxItem!!)
                    return OrderValidationResult(
                        successful = false,
                        errorMessage = "Please select maximum ${modifier.maxItem} modifier item for ${modifier.name}!"
                    )
            }
        }
        return OrderValidationResult(
            successful = true
        )
    }
}