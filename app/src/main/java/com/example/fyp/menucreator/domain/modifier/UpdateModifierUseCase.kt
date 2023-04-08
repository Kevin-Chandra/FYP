package com.example.fyp.menucreator.domain.modifier

import android.net.Uri
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.modifierItem.AddModifierItemUseCase
import com.example.fyp.menucreator.domain.modifierItem.DeleteModifierItemUseCase
import com.example.fyp.menucreator.domain.productImage.UploadImageUseCase
import com.example.fyp.menucreator.util.MenuCreatorResponse
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class UpdateModifierUseCase @Inject constructor(
    private val modifierRepository: ModifierRepository,
    private val uploadImageUseCase: UploadImageUseCase,
    private val deleteModifierItemUseCase: DeleteModifierItemUseCase,
    private val addModifierItemUseCase: AddModifierItemUseCase,
) {
    suspend operator fun invoke(
        modifier: Modifier,
        itemList: List<ModifierItem>,
        image: Uri?,
        result: (UiState<String>) -> Unit
    ) {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            result.invoke(UiState.Failure(throwable as Exception))
            context.cancelChildren()
        }
        val parentJob = CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            try {
                var imgPath: Deferred<Pair<String, String>?>? = null
                var listToBeDeleted : List<String> = emptyList()
                val getItemListJob = launch {
                    listToBeDeleted = modifierRepository.getModifierList(modifier.productId)?: emptyList()
                }
                println("$image and ${modifier.imageUri}")
                if (image != null && image.toString() != modifier.imageUri) {
                    imgPath = async {
                        uploadImageUseCase(ProductType.Modifier, modifier.productId, image) {
                            when (it) {
                                is UiState.Failure -> {
                                    throw it.e!!
                                }
                                else -> {}
                            }
                        }
                    }
                }
                val deleteItemsJob = launch {
                    getItemListJob.join()
                    println(listToBeDeleted)
                    for (i in listToBeDeleted){
                        println("deleting $i...")
                        launch { deleteModifierItemUseCase(i){} }
                    }
                }
                val count = AtomicInteger(0)
                val itemJob = launch {
                    deleteItemsJob.children.forEach {
                        it.join()
                        println(it.toString() + "finish")
                    }
                    deleteItemsJob.join()
                    println("item job started")
                    for (item in itemList) {
                        launch(Dispatchers.IO) {
                            ensureActive()
                            addModifierItemUseCase(item) {
                                when (it) {
                                    is UiState.Failure -> {
                                        throw it.e!!
                                    }
                                    UiState.Loading -> {}
                                    is UiState.Success -> {
                                        count.incrementAndGet()
                                        println(it)
                                        println("count = " + count.get())
                                    }
                                }
                            }
                        }
                    }
                }
                val modifierJob = launch {
                    getItemListJob.join()
                    ensureActive()
                    println("update mod job started")
                    modifierRepository.updateModifier(modifier) {
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
                    if (image != null  && image.toString() != modifier.imageUri) {
                        modifierJob.join()
                        modifierRepository.updateImageField(modifier.productId, imgPath?.await()) {
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
                itemJob.children.forEach {
                    it.join()
                    println("child job Joined")
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw CancellationException()
                }
                result.invoke(UiState.Failure(e))
                return@launch
            }
        }
        parentJob.invokeOnCompletion {
            println("parent ioc")
            if (it != null) {
                result.invoke(UiState.Failure(it as Exception))
            } else {
                result.invoke(UiState.Success(MenuCreatorResponse.MODIFIER_UPDATE_SUCCESS))
            }
        }
    }
}