package com.example.fyp.menucreator.domain

import com.example.fyp.menucreator.data.repository.MenuSettingsRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class DeleteFoodCategoryUseCase @Inject constructor(
    private val repo: MenuSettingsRepository
) {

    suspend operator fun invoke(id: String, result: (UiState<Boolean>) -> Unit){
        repo.deleteCategory(id,result)
    }
}