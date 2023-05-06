package com.example.fyp.menucreator.domain.modifierItem

import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class GetModifierItemByReturnUseCase @Inject constructor(
    private val modifierItemRepository: ModifierItemRepository
) {
    suspend operator fun invoke(id:String) =
        modifierItemRepository.getModifierItem(id)
}