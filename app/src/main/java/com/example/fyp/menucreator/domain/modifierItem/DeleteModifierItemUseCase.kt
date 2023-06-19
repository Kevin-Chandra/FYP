package com.example.fyp.menucreator.domain.modifierItem

import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class DeleteModifierItemUseCase @Inject constructor(
    private val modifierItemRepository: ModifierItemRepository
) {
    suspend operator fun invoke(id:String, result:(UiState<String>) -> Unit){
        modifierItemRepository.deleteModifierItem(id,result)
    }
}