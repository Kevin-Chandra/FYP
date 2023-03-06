package com.example.fyp.menucreator.domain

import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.data.repository.MenuSettingsRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class UpdateFoodCategoryUseCase @Inject constructor(
    private val repo: MenuSettingsRepository
) {

    suspend operator fun invoke(category: FoodCategory, result: (UiState<Boolean>) -> Unit){
        repo.updateCategory(category,result)
    }
}