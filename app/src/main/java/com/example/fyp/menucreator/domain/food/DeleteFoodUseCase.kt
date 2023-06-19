package com.example.fyp.menucreator.domain.food

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.domain.productImage.DeleteImageUseCase
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class DeleteFoodUseCase @Inject constructor(
    private val getFoodUseCase: GetFoodUseCase,
    private val foodRepo: FoodRepository,
    private val deleteImageUseCase: DeleteImageUseCase
) {
    suspend operator fun invoke(account: Account, foodId: String, result:(UiState<String>) -> Unit){
        if (account.accountType != AccountType.Admin && account.accountType != AccountType.Manager){
            result.invoke(UiState.Failure(Exception("You don't have permission")))
            return
        }
        getFoodUseCase(foodId){
            when (it){
                is UiState.Success -> {
                    if (!it.data.imagePath.isNullOrEmpty())
                        deleteImageUseCase(it.data.imagePath,result)
                }
                else -> {}
            }
        }
        foodRepo.deleteFood(foodId,result)
    }
}