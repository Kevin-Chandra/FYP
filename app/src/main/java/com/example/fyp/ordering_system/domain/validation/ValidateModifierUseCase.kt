package com.example.fyp.ordering_system.domain.validation

import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem

class ValidateModifierUseCase {
    operator fun invoke(modifier: Modifier, list: List<ModifierItem>): OrderValidationResult {
        if (modifier.required && list.isEmpty()){
            return OrderValidationResult(
                successful = false,
                errorMessage = "Please select modifier item for ${modifier.name}!"
            )
        }
        if (modifier.multipleChoice){
            if (modifier.minItem!! > list.size)
                return OrderValidationResult(
                    successful = false,
                    errorMessage = "Please select minimum ${modifier.minItem} modifier item for ${modifier.name}!"
                )
            if (list.size > modifier.maxItem!!)
                return OrderValidationResult(
                    successful = false,
                    errorMessage = "Please select maximum ${modifier.maxItem} modifier item for ${modifier.name}!"
                )
        }
        return OrderValidationResult(
            successful = true
        )
    }
}