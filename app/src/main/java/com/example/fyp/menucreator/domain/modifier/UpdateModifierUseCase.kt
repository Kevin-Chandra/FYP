package com.example.fyp.menucreator.domain.modifier

import android.net.Uri
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.modifierItem.AddModifierItemUseCase
import com.example.fyp.menucreator.domain.modifierItem.DeleteModifierItemUseCase
import com.example.fyp.menucreator.domain.modifierItem.UpdateModifierItemUseCase
import com.example.fyp.menucreator.domain.productImage.UploadImageUseCase
import com.example.fyp.menucreator.util.MenuCreatorResponse
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.*
import javax.inject.Inject

class UpdateModifierUseCase @Inject constructor(
    private val modifierRepository: ModifierRepository,
    private val uploadImageUseCase: UploadImageUseCase,
    private val deleteModifierItemUseCase: DeleteModifierItemUseCase,
    private val addModifierItemUseCase: AddModifierItemUseCase,
    private val updateModifierItemUseCase: UpdateModifierItemUseCase
) {
    suspend operator fun invoke(
        account: Account,
        modifier: Modifier,
        itemList: List<ModifierItem>,
        image: Uri?,
        result: (UiState<String>) -> Unit
    ) {
        if (account.accountType != AccountType.Admin && account.accountType != AccountType.Manager){
            result.invoke(UiState.Failure(Exception("You don't have permission")))
            return
        }
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            result.invoke(UiState.Failure(throwable as Exception))
            context.cancelChildren()
        }
        val parentJob = CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
            try {
                var imgPath: Deferred<Pair<String, String>?>? = null
                var oldList = mutableListOf<String>()
                val toUpdateList = mutableListOf<ModifierItem>()
                val toAddList = mutableListOf<ModifierItem>()
                val getItemListJob = launch {
                    oldList = modifierRepository.getModifierList(modifier.productId)?.toMutableList()?: mutableListOf()
                }
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
                getItemListJob.join()
                for (i in itemList){
                    if (oldList.contains(i.productId)){
                        oldList.remove(i.productId)
                        toUpdateList.add(i)
                    } else {
                        toAddList.add(i)
                    }
                }
                val deleteItemsJob = launch {
                    for (i in oldList){
                        println("deleting $i...")
                        launch { deleteModifierItemUseCase(i){} }
                    }
                }
                val addItemJob = launch {
                    println("add item job started")
                    for (item in toAddList) {
                        launch(Dispatchers.IO) {
                            ensureActive()
                            println("add $item")
                            addModifierItemUseCase(item) {
                                when (it) {
                                    is UiState.Failure -> {
                                        throw it.e!!
                                    }
                                    UiState.Loading -> {}
                                    is UiState.Success -> {
                                        println(it)
                                    }
                                }
                            }
                        }
                    }
                }
                val updateItemJob = launch{
                    for (i in toUpdateList){
                        launch(Dispatchers.IO){
                            ensureActive()
                            println("update $i")
                            updateModifierItemUseCase(i){
                                when (it) {
                                    is UiState.Failure -> {
                                        throw it.e!!
                                    }
                                    UiState.Loading -> {}
                                    is UiState.Success -> {
                                        println(it)
                                    }
                                }
                            }
                        }
                    }
                }
                val modifierJob = launch {
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
                val updateImageFieldJob = launch {
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
                addItemJob.children.forEach {
                    it.join()
                    println("add child job Joined")
                }
                updateItemJob.children.forEach {
                    it.join()
                    println("update child job Joined")
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