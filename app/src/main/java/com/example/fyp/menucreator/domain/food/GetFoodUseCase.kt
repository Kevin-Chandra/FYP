package com.example.fyp.menucreator.domain.food

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoodUseCase @Inject constructor(
    private val foodRepo: FoodRepository
) {
    suspend operator fun invoke(id:String, result:(UiState<Food>) -> Unit){
        foodRepo.getFood(id,result)
    }
}