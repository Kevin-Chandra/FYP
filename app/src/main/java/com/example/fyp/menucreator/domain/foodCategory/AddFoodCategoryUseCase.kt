package com.example.fyp.menucreator.domain.foodCategory

import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.data.repository.MenuSettingsRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class AddFoodCategoryUseCase @Inject constructor(
    private val categoryRepo: MenuSettingsRepository
) {
    suspend operator fun invoke(category: FoodCategory, result:(UiState<Boolean>) -> Unit){
        if (!categoryRepo.checkCategory(category.name)){
            result.invoke(UiState.Failure(Exception("Category already exist!")))
            return
        }
        categoryRepo.addCategory(category,result)
    }
}