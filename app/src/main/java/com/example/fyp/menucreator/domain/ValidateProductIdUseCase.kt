package com.example.fyp.menucreator.domain

import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.model.ProductType.*
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import javax.inject.Inject

class ValidateProductIdUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
    private val modifierRepository: ModifierRepository,
    private val modifierItemRepository: ModifierItemRepository
) {

    suspend operator fun invoke(id: String, productType: ProductType) : ProductValidationResult{
        if (id.isBlank()){
            return ProductValidationResult(
                errorMessage = "Product ID is blank",
                successful = false
            )
        }
        if (id.isEmpty()){
            return ProductValidationResult(
                errorMessage = "Product ID is empty",
                successful = false
            )
        }
        when (productType){
            FoodAndBeverage -> {
                if (foodRepository.checkFoodId(id))
                    return ProductValidationResult(
                        errorMessage = "Food ID already exist!",
                        successful = false
                    )
            }
            Modifier -> {
                if (modifierRepository.checkModifierId(id))
                    return ProductValidationResult(
                        errorMessage = "Modifier ID already exist!",
                        successful = false
                    )
            }
            ModifierItem -> {
                if (modifierItemRepository.checkModifierId(id))
                    return ProductValidationResult(
                        errorMessage = "Item ID already exist!",
                        successful = false
                    )
            }
        }
        return ProductValidationResult(
            successful = true
        )
    }
}