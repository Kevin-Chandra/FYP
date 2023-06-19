package com.example.fyp.menucreator.domain

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.util.UiState
import javax.inject.Inject

class UpdateProductAvailabilityUseCase @Inject constructor(
    private val foodRepo: FoodRepository,
    private val itemRepository: ModifierItemRepository

) {
    suspend operator fun invoke(account: Account, id:String, type: ProductType,value: Boolean, result:(UiState<String>) -> Unit){
        if (account.accountType != AccountType.Staff && account.accountType != AccountType.Manager && account.accountType != AccountType.Admin){
            result.invoke(UiState.Failure(Exception("You don't have permission")))
            return
        }
        if (account.accountType == AccountType.Staff && (account.staffPosition == StaffPosition.Disabled || account.staffPosition == StaffPosition.Pending)){
            result.invoke(UiState.Failure(Exception("Your staff account is ${account.staffPosition.toString().lowercase()}!")))
            return
        }
        when(type){
            ProductType.FoodAndBeverage -> {
                foodRepo.updateAvailability(id,value,result)
            }
            ProductType.Modifier -> {
            }
            ProductType.ModifierItem -> {
                itemRepository.updateAvailability(id,value,result)
            }
        }
    }
}