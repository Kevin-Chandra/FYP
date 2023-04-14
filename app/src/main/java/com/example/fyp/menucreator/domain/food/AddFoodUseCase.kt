package com.example.fyp.menucreator.domain.food

import android.net.Uri
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.domain.productImage.UploadImageUseCase
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.*
import javax.inject.Inject

class AddFoodUseCase @Inject constructor(
    private val foodRepo: FoodRepository,
    private val uploadImageUseCase: UploadImageUseCase,
    private val deleteFoodUseCase: DeleteFoodUseCase
) {
    suspend operator fun invoke(account: Account, food: Food, image: Uri?, result:(UiState<String>) -> Unit){
        if (account.accountType != AccountType.Admin && account.accountType != AccountType.Manager){
            result.invoke(UiState.Failure(Exception("You don't have permission")))
            return
        }
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            result.invoke(UiState.Failure(throwable as Exception))
            context.cancelChildren()
            val deletionJob = CoroutineScope(Dispatchers.IO).launch {
                deleteFoodUseCase(account,food.productId){}
            }
            deletionJob.invokeOnCompletion {
                result.invoke(UiState.Success("Deleted failed data"))
            }
        }
         val parentJob = CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            try {
                var imgPath: Deferred<Pair<String, String>?>? = null
                if (image != null) {
                    imgPath = async {
                        uploadImageUseCase(ProductType.FoodAndBeverage, food.productId, image) {
                            when (it) {
                                is UiState.Failure -> {
                                    throw it.e!!
                                }
                                else -> {}
                            }
                        }
                    }
                }
                val foodJob = launch {
                    ensureActive()
                    foodRepo.addFood(food) {
                        when (it) {
                            is UiState.Failure -> {
                                throw it.e!!
                            }
                            else -> {}
                        }
                    }
                }
                launch {
                    ensureActive()
                    if (image != null) {
                        foodJob.join()
                        foodRepo.updateImageField(food.productId, imgPath?.await()) {
                            when (it) {
                                is UiState.Failure -> {
                                    throw it.e!!
                                }
                                else -> {
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception){
                if (e is CancellationException){
                    throw CancellationException()
                }
                result.invoke(UiState.Failure(e))
                return@launch
            }
        }
        parentJob.invokeOnCompletion {
            if (it != null) {
                result.invoke(UiState.Failure(it as Exception))
            } else {
                result.invoke(UiState.Success("Add Food Success"))
            }
        }
    }
}