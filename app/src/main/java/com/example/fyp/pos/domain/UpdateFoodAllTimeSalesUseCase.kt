package com.example.fyp.pos.domain

import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class UpdateFoodAllTimeSalesUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
) {
    suspend operator fun invoke(foodId:String,quantity: Int, result: (UiState<String>) -> Unit) {
        foodRepository.updateFoodAllTimeSales(foodId,quantity.toLong(),result)
    }
}