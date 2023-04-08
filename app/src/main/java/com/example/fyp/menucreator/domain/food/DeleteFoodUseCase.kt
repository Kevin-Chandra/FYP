package com.example.fyp.menucreator.domain.food

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ProductImageRepository
import com.example.fyp.menucreator.domain.productImage.DeleteImageUseCase
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class DeleteFoodUseCase @Inject constructor(
    private val getFoodUseCase: GetFoodUseCase,
    private val foodRepo: FoodRepository,
    private val deleteImageUseCase: DeleteImageUseCase
) {
    suspend operator fun invoke(foodId: String, result:(UiState<String>) -> Unit){
        getFoodUseCase(foodId){
            when (it){
                is UiState.Success -> {
                    if (!it.data.imagePath.isNullOrEmpty())
                        deleteImageUseCase(it.data.imagePath,result)
                }
                else -> {}
            }
        }
        foodRepo.deleteFood(foodId,result)
    }
}