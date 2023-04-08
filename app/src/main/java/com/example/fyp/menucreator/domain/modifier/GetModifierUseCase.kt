package com.example.fyp.menucreator.domain.modifier

import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class GetModifierUseCase @Inject constructor(
    private val modifierRepository: ModifierRepository
) {
    suspend operator fun invoke(id:String, result:(UiState<Modifier?>) -> Unit){
        modifierRepository.getModifier(id,result)
    }
}