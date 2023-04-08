package com.example.fyp.menucreator.domain.modifierItem

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetModifierItemListUseCase @Inject constructor(
    private val modifierItemRepository: ModifierItemRepository
) {
    operator fun invoke(result:(Flow<UiState<List<ModifierItem>>>) -> Unit){
        result.invoke(modifierItemRepository.subscribeModifierItemUpdates())
    }
}