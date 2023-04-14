package com.example.fyp.menucreator.domain.modifier

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.modifierItem.DeleteModifierItemUseCase
import com.example.fyp.menucreator.domain.productImage.DeleteImageUseCase
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.*
import javax.inject.Inject

class DeleteModifierUseCase @Inject constructor(
    private val modifierRepository: ModifierRepository,
    private val getModifierUseCase: GetModifierUseCase,
    private val deleteModifierItemUseCase: DeleteModifierItemUseCase,
    private val deleteImageUseCase: DeleteImageUseCase,
) {
    suspend operator fun invoke(account: Account, id:String, result:(UiState<String>) -> Unit){
        if (account.accountType != AccountType.Admin && account.accountType != AccountType.Manager){
            result.invoke(UiState.Failure(Exception("You don't have permission")))
            return
        }
        val jobs = mutableListOf<Job>()
        getModifierUseCase(id) { it ->
            if (it is UiState.Success && it.data != null) {
                it.data.imagePath?.let {
                    deleteImageUseCase(it,result)
                }
                it.data.modifierItemList.forEach { it1 ->
                    jobs.add(CoroutineScope(Dispatchers.IO).launch {
                        deleteModifierItemUseCase(
                            it1,
                            result
                        )
                    })
                }
            }
        }
        jobs.joinAll()
        modifierRepository.deleteModifier(id,result)
    }
}