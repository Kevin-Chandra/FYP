package com.example.fyp.menucreator.domain.food

import android.net.Uri
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ProductImageRepository
import com.example.fyp.menucreator.domain.UploadImageUseCase
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.*
import javax.inject.Inject

class AddFoodUseCase @Inject constructor(
    private val foodRepo: FoodRepository,
    private val uploadImageUseCase: UploadImageUseCase
) {
    suspend operator fun invoke(food: Food, image: Uri?, result:(UiState<String>) -> Unit){
         val parentJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                var imgPath: Deferred<Pair<String, String>?>? = null
                if (image != null) {
                    imgPath = async {
                        uploadImageUseCase(food.productId,image) {
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
                    foodRepo.addFood(food){
                        when (it) {
                            is UiState.Failure -> {
                                throw it.e!!
                            }
                            else ->{
                                result.invoke(it)
                            }
                        }
                    }
                }
                launch {
                    println("imageJob")
                    if (image != null) {
                        foodJob.join()
                        foodRepo.updateImageField(food.productId, imgPath?.await()) {
                            when (it) {
                                is UiState.Failure -> {
                                    throw it.e!!
                                }
                                else -> {
                                    println("finish img field")
//                                        result.invoke(it)
                                }
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception){
                result.invoke(UiState.Failure(e))
                return@launch
            }
        }
        parentJob.invokeOnCompletion {
            println("Completed all")
            result.invoke(UiState.Success("Add Food Success")) }
    }
}