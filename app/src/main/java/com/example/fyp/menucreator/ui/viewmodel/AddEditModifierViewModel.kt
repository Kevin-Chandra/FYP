package com.example.fyp.menucreator.ui.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.domain.use_case.ValidateNameUseCase
import com.example.fyp.account_management.domain.use_case.ValidationResult
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.ProductValidationResult
import com.example.fyp.menucreator.domain.modifier.AddModifierUseCase
import com.example.fyp.menucreator.domain.modifier.GetModifierUseCase
import com.example.fyp.menucreator.domain.modifier.UpdateModifierUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemListUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemUseCase
import com.example.fyp.menucreator.domain.validation.ValidateDuplicateIdUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductIdUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductNameUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductPriceUseCase
import com.example.fyp.menucreator.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.arrayListOf
import kotlin.collections.get
import kotlin.collections.iterator
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AddEditModifierViewModel @Inject constructor(
    private val addModifierUseCase: AddModifierUseCase,
    private val getModifierUseCase: GetModifierUseCase,
    private val getModifierItemListUseCase: GetModifierItemListUseCase,
    private val updateModifierUseCase: UpdateModifierUseCase,
    private val validateProductNameUseCase: ValidateProductNameUseCase,
    private val validateProductIdUseCase: ValidateProductIdUseCase,
    private val validateProductPriceUseCase: ValidateProductPriceUseCase,
    private val validateDuplicateIdUseCase: ValidateDuplicateIdUseCase,
) : ViewModel() {

    private var _productId: String? = null
    val productId get() = _productId!!

    private var _modifier: Modifier? = null
    val modifier get() = _modifier!!

    private val _loadResponse = MutableStateFlow<UiState<String>>(UiState.Loading)
    val loadResponse = _loadResponse.asStateFlow()

    private val items = MutableStateFlow<UiState<List<ModifierItem>>>(UiState.Loading)

    private val loadModifier = MutableStateFlow<UiState<Modifier?>>(UiState.Loading)

    private val modifierItemMap = mutableMapOf<String, ModifierItem>()

    private val _addEditModifierState = MutableStateFlow(AddEditModifierState())
    val addEditModifierState = _addEditModifierState.asStateFlow()

    private val _addEditModifierResponse = MutableStateFlow<UiState<String>>(UiState.Success(""))
    val addEditModifierResponse = _addEditModifierResponse.asStateFlow()

    fun initialize(id: String) {
        _loadResponse.value = UiState.Loading
        _productId = id
        if (_modifier == null)
            load()
        else
            _loadResponse.value = UiState.Success("Modifier Loaded")
    }
    fun onEvent(event: AddEditModifierEvent) {
        println(event)
        when(event) {
            is AddEditModifierEvent.ImageChanged -> {
                _addEditModifierState.value = _addEditModifierState.value.copy(image = event.image)
            }
            is AddEditModifierEvent.ItemListChanged -> {
                _addEditModifierState.value = _addEditModifierState.value.copy(itemList = event.items)
            }
            is AddEditModifierEvent.ItemErrorListChanged -> {
                _addEditModifierState.value = _addEditModifierState.value.copy(itemErrorList = event.itemErrors)
            }
            is AddEditModifierEvent.MultipleChoiceChanged -> {
                _addEditModifierState.value = _addEditModifierState.value.copy(isMultipleChoice = event.multipleChoice)
            }
            is AddEditModifierEvent.NameChanged -> {
                _addEditModifierState.value = _addEditModifierState.value.copy(name = event.name)
            }
            is AddEditModifierEvent.ProductIdChanged -> {
                _addEditModifierState.value = _addEditModifierState.value.copy(productId = event.id)
            }
            is AddEditModifierEvent.RequiredChanged -> {
                _addEditModifierState.value = _addEditModifierState.value.copy(isRequired = event.required)
            }
            is AddEditModifierEvent.Save -> {
                submit(event.isEdit,event.account)
            }
        }
    }

    private fun submit(isEdit: Boolean,account: Account) = viewModelScope.launch{
        _addEditModifierResponse.value = UiState.Loading
        var idResult = ProductValidationResult(successful = true)
        if (!isEdit){
            idResult = validateProductIdUseCase(addEditModifierState.value.productId, ProductType.Modifier)
        }
        val nameResult = validateProductNameUseCase(addEditModifierState.value.name)
        val validationResultList = mutableListOf(idResult,nameResult)

        val errorList = mutableListOf<Triple<String?,String?,String?>?>()

        for (i in addEditModifierState.value.itemList){
            var idRes = ProductValidationResult(successful = true)
            if (!i.first.second){
                idRes = validateProductIdUseCase(i.first.first,ProductType.ModifierItem)
            }
            val nameRes = validateProductNameUseCase(i.second)
            val priceRes = validateProductPriceUseCase(i.third)
            validationResultList.add(idRes)
            validationResultList.add(nameRes)
            validationResultList.add(priceRes)

            errorList.add(
                if (listOf(idRes,nameRes,priceRes).any{!it.successful}) {
                    Triple(
                        if (idRes.successful) null else idRes.errorMessage,
                        if (nameRes.successful) null else nameRes.errorMessage,
                        if (priceRes.successful) null else priceRes.errorMessage
                    )
                } else {
                    null
                }
            )
        }

        if (addEditModifierState.value.itemList.isEmpty()){
            _addEditModifierResponse.value = UiState.Failure(IllegalArgumentException("Modifier items is empty!"))
            return@launch
        }

        val duplicateIdResult = validateDuplicateIdUseCase(addEditModifierState.value.itemList.map { it.first.first }.toList())
        validationResultList.add(duplicateIdResult)

        if (addEditModifierState.value.isMultipleChoice && addEditModifierState.value.itemList.size < 2){
            _addEditModifierResponse.value = UiState.Failure(IllegalArgumentException("Modifier items must be at least 2 for multiple choice!"))
            return@launch
        }

        _addEditModifierState.value = _addEditModifierState.value.copy(
            itemErrorList = errorList
        )


        if (validationResultList.any { !it.successful }){
            _addEditModifierState.value = _addEditModifierState.value.copy(
                productIdError = idResult.errorMessage,
                nameError = nameResult.errorMessage
            )
            if (!duplicateIdResult.successful) {
                _addEditModifierResponse.value = UiState.Failure(IllegalArgumentException("Duplicate Modifier Item Id!"))
                return@launch
            } else {
                _addEditModifierResponse.value = UiState.Failure(IllegalArgumentException("Field(s) error!"))
            }
            return@launch
        } else {
            _addEditModifierState.value.copy(
                productIdError = null,
                nameError = null
            )
        }

        if (isEdit){
            updateModifier(account)
        } else {
            addModifier(account)
        }

    }

    private fun addModifier(account: Account) = viewModelScope.launch(Dispatchers.IO){
        val itemStringList = mutableListOf<String>()
        val itemList = mutableListOf<ModifierItem>()
        for (i in addEditModifierState.value.itemList){
            itemList.add(getModifierItem(i.first.first,i.second,i.third))
            itemStringList.add(i.first.first)
        }

        val modifier = getModifier(
            addEditModifierState.value.productId,
            addEditModifierState.value.name,
            addEditModifierState.value.isMultipleChoice,
            addEditModifierState.value.isRequired,
            itemStringList.toList(),
            account.id,
            account.id
        )

        addModifierUseCase.invoke(account, modifier,itemList,addEditModifierState.value.image){
            _addEditModifierResponse.value = it
        }
    }

    private fun updateModifier(account: Account) = viewModelScope.launch(Dispatchers.IO){
        val itemStringList = mutableListOf<String>()
        val itemList = mutableListOf<ModifierItem>()
        for (i in addEditModifierState.value.itemList){
            itemList.add(getModifierItem(i.first.first,i.second,i.third))
            itemStringList.add(i.first.first)
        }

        val newModifier = modifier.copy(
            productId = addEditModifierState.value.productId,
            name = addEditModifierState.value.name,
            multipleChoice = addEditModifierState.value.isMultipleChoice,
            required = addEditModifierState.value.isRequired,
            modifierItemList = itemStringList.toList(),
            lastUpdated = Date(),
            lastUpdatedBy = account.id
        )

        updateModifierUseCase.invoke(account, newModifier,itemList,addEditModifierState.value.image){
            _addEditModifierResponse.value = it
        }
    }



    private fun getModifier(
        productId: String,
        name: String,
        isMultipleChoice: Boolean,
        isRequired: Boolean,
        itemList: List<String>,
        createdBy: String,
        lastUpdatedBy: String
    ): Modifier {
        return Modifier(productId, name, isMultipleChoice, isRequired, itemList,null,null,null,Date(),createdBy,lastUpdatedBy)
    }

    private fun getModifierItem(
        productId: String,
        name: String,
        price: String,
    ): ModifierItem {
        return ModifierItem(productId, name, price.toDoubleOrNull() ?: -1.0)
    }

    private fun load() = viewModelScope.launch{
        getModifierUseCase(productId) {
            loadModifier.value = it
        }
        getModifierItemListUseCase.invoke {
            it.onEach { state ->
                items.value = state
            }.launchIn(viewModelScope)
        }

        combine(loadModifier,items){ modifier,items ->
            if (modifier is UiState.Success){
                _modifier = modifier.data
            }
            if (items is UiState.Success){
                for (i in items.data){
                    modifierItemMap[i.productId] = i
                }
            }
            if (modifier is UiState.Success && items is UiState.Success){
                loadToState()
                _loadResponse.value = UiState.Success("Load Success")
            }
        }.stateIn(viewModelScope)
    }

    private fun getItem(id: String,isEdit: Boolean) : Triple<Pair<String,Boolean>,String,String>{
        val item = modifierItemMap[id]
        return Triple(
            Pair (item?.productId?:"",isEdit),
            item?.name?:"",
            item?.price?.toString()?:""
        )
    }

    private fun loadToState() {
        onEvent(AddEditModifierEvent.ProductIdChanged(modifier.productId))
        onEvent(AddEditModifierEvent.NameChanged(modifier.name))
        onEvent(AddEditModifierEvent.RequiredChanged(modifier.required))
        onEvent(AddEditModifierEvent.MultipleChoiceChanged(modifier.multipleChoice))
        onEvent(AddEditModifierEvent.ImageChanged(modifier.imageUri?.toUri()))
        val list = mutableListOf<Triple<Pair<String,Boolean>,String,String>>()
        val errorList = mutableListOf<Triple<String?,String?,String?>?>()
        for (i in modifier.modifierItemList){
            list.add(getItem(i,true))
            errorList.add(null)
        }
        onEvent(AddEditModifierEvent.ItemListChanged(list))
        onEvent(AddEditModifierEvent.ItemErrorListChanged(errorList))
    }


    fun reset() {
        _addEditModifierState.value = AddEditModifierState()
    }


}