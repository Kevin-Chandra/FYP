package com.example.fyp.menucreator.domain.food

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoodListUseCase @Inject constructor(
    private val foodRepo: FoodRepository
) {
    operator fun invoke(result:(Flow<UiState<List<Food>>>) -> Unit){
        result.invoke(foodRepo.subscribeFoodUpdates())
    }
}