package com.example.fyp.menucreator.domain.modifierItem

import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import javax.inject.Inject

class GetModifierItemByReturnUseCase @Inject constructor(
    private val modifierItemRepository: ModifierItemRepository
) {
    suspend operator fun invoke(id:String) =
        modifierItemRepository.getModifierItem(id)
}