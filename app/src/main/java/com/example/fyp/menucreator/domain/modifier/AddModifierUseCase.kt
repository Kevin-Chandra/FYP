package com.example.fyp.menucreator.domain.modifier

import android.net.Uri
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.modifierItem.AddModifierItemUseCase
import com.example.fyp.menucreator.domain.productImage.UploadImageUseCase
import com.example.fyp.menucreator.util.MenuCreatorResponse
import com.example.fyp.menucreator.util.UiState
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class AddModifierUseCase @Inject constructor(
    private val modifierRepository: ModifierRepository,
    private val uploadImageUseCase: UploadImageUseCase,
    private val deleteModifierUseCase: DeleteModifierUseCase,
    private val addModifierItemUseCase: AddModifierItemUseCase,
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
            val deletionJob = CoroutineScope(Dispatchers.IO).launch {
                deleteModifierUseCase(account,modifier.productId) {}
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
                val count = AtomicInteger(0)
                val itemJob = launch {
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
                                    }
                                }
                            }
                        }
                    }
                }
                val modifierJob = launch {
                    ensureActive()
                    modifierRepository.addModifier(modifier) {
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
            if (it != null) {
                result.invoke(UiState.Failure(it as Exception))
            } else {
                result.invoke(UiState.Success(MenuCreatorResponse.MODIFIER_ADD_SUCCESS))
            }
        }
    }
}