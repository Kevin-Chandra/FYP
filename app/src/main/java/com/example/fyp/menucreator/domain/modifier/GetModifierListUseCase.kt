package com.example.fyp.menucreator.domain.modifier

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetModifierListUseCase @Inject constructor(
    private val modifierRepository: ModifierRepository
) {
    operator fun invoke(result:(Flow<UiState<List<Modifier>>>) -> Unit){
        result.invoke(modifierRepository.subscribeModifierUpdates())
    }
}