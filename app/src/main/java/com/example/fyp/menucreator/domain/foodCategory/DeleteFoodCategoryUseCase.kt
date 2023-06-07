package com.example.fyp.menucreator.domain.foodCategory

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.menucreator.data.repository.MenuSettingsRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class DeleteFoodCategoryUseCase @Inject constructor(
    private val repo: MenuSettingsRepository
) {
    suspend operator fun invoke(account: Account, id: String, result: (UiState<Boolean>) -> Unit){
        if (account.accountType !in listOf(AccountType.Manager, AccountType.Admin)){
            result.invoke(UiState.Failure(Exception("Account has no permission!")))
            return
        }
        repo.deleteCategory(id,result)
    }
}