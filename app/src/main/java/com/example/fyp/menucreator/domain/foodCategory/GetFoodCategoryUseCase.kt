package com.example.fyp.menucreator.domain.foodCategory

import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.data.repository.MenuSettingsRepository
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoodCategoryUseCase @Inject constructor(
    private val repo: MenuSettingsRepository
) {
    operator fun invoke() : Flow<UiState<List<FoodCategory>>> {
        return repo.getFoodCategory()
    }
}