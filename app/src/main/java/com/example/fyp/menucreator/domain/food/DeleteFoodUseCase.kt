package com.example.fyp.menucreator.domain.food

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class DeleteFoodUseCase @Inject constructor(
    private val foodRepo: FoodRepository
) {
    suspend operator fun invoke(foodId: String, result:(UiState<Boolean>) -> Unit){
        foodRepo.deleteFood(foodId,result)
    }
}