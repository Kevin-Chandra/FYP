package com.example.fyp.menucreator.domain.modifierItem

import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class AddModifierItemUseCase @Inject constructor(
    private val modifierItemRepository: ModifierItemRepository
) {
    suspend operator fun invoke(item: ModifierItem, result:(UiState<String>) -> Unit){
        modifierItemRepository.addModifierItem(item, result)
    }
}