package com.example.fyp.menucreator.ui.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.domain.use_case.GetAccountUseCase
import com.example.fyp.account_management.domain.use_case.GetSessionUseCase
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.*
import com.example.fyp.menucreator.domain.food.AddFoodUseCase
import com.example.fyp.menucreator.domain.food.GetFoodUseCase
import com.example.fyp.menucreator.domain.food.UpdateFoodUseCase
import com.example.fyp.menucreator.domain.modifier.GetModifierListUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductCategoryUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductIdUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductNameUseCase
import com.example.fyp.menucreator.domain.validation.ValidateProductPriceUseCase
import com.example.fyp.menucreator.util.AddEditFoodEvent
import com.example.fyp.menucreator.util.AddEditFoodState
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import javax.inject.Inject

// This view model assist in view food details, add new food, edit food. Two fragments is under
// this view model which is AddEditFoodFragment and SecondFragment
@HiltViewModel
class AddEditFoodViewModel @Inject constructor(
    private val addFoodUseCase: AddFoodUseCase,
    private val editFoodUseCase: UpdateFoodUseCase,
    private val getFoodUseCase: GetFoodUseCase,
    private val validateProductIdUseCase: ValidateProductIdUseCase,
    private val validateProductNameUseCase: ValidateProductNameUseCase,
    private val validateProductPriceUseCase: ValidateProductPriceUseCase,
    private val validateProductCategoryUseCase: ValidateProductCategoryUseCase,
    private val getModifierListUseCase: GetModifierListUseCase,

) : ViewModel() {

    private var productId: String? = null

    private var _food: Food? = null
    val food: Food
        get() = _food!!

    private val _modifiers = MutableStateFlow<UiState<List<Modifier>>>(UiState.Loading)
    val modifiers = _modifiers.asStateFlow()

    private var _modifierMap: MutableMap<String, Modifier> = mutableMapOf()
    val modifierMap: Map<String, Modifier> get() = _modifierMap

    private val _foodLoaded = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val foodLoaded = _foodLoaded.asStateFlow()

    //-----------------------------------------------------

    private val _addEditFoodState = MutableStateFlow(AddEditFoodState())
    val addEditFoodState = _addEditFoodState.asStateFlow()

    private val _addEditFoodResponse = MutableStateFlow<UiState<String>>(UiState.Success(""))
    val addEditFoodResponse = _addEditFoodResponse.asStateFlow()

    init {
        getModifierList()
    }

    fun initialize(id: String) {
        _foodLoaded.value = UiState.Loading
        productId = id
        if (_food == null){
            loadFood()
        } else {
            _foodLoaded.value = UiState.Success(true)
        }

    }

    private fun loadFood() = viewModelScope.launch{
        getFoodUseCase.invoke(productId!!){
            when(it){
                is UiState.Success -> {
                    _food = it.data
                    loadState()
                    _foodLoaded.value = UiState.Success(true)
                }
                is UiState.Failure -> {
                    _foodLoaded.value = UiState.Failure(it.e)
                }
                is UiState.Loading -> {}
            }
        }
    }

    fun onEvent(event: AddEditFoodEvent) {
        when(event) {
            is AddEditFoodEvent.DescriptionChanged -> {
                _addEditFoodState.value = _addEditFoodState.value.copy(description = event.description)
            }
            is AddEditFoodEvent.FoodCategoryChanged -> {
                _addEditFoodState.value = _addEditFoodState.value.copy(foodCategory = event.category)
            }
            is AddEditFoodEvent.ImageChanged -> {
                _addEditFoodState.value = _addEditFoodState.value.copy(image = event.image)
            }
            is AddEditFoodEvent.ModifiableChanged -> {
                _addEditFoodState.value = _addEditFoodState.value.copy(isModifiable = event.isModifiable)
            }
            is AddEditFoodEvent.ModifierChanged -> {
                _addEditFoodState.value = _addEditFoodState.value.copy(modifierList = event.modifierList)
            }
            is AddEditFoodEvent.NameChanged -> {
                _addEditFoodState.value = _addEditFoodState.value.copy(name = event.name)
            }
            is AddEditFoodEvent.PriceChanged -> {
                _addEditFoodState.value = _addEditFoodState.value.copy(price = event.price)
            }
            is AddEditFoodEvent.ProductIdChanged -> {
                _addEditFoodState.value = _addEditFoodState.value.copy(productId = event.id)
            }
            is AddEditFoodEvent.Save -> {
                submit(event.isEdit,event.account)
            }
        }
    }

    private fun getFood(
        productId: String,
        name: String,
        price: String,
        description: String,
        category: String,
        isModifiable: Boolean,
        imageUri: String?,
        imagePath: String?,
        modifierList: List<String>?,
        createdDate: Date = Date(),
        createdBy: String,
        lastUpdatedBy: String
    ): Food {
        return Food(
            productId = productId,
            name = name,
            price = price.toDouble(),
            description = description,
            category = category,
            modifiable = isModifiable,
            imagePath = imagePath,
            imageUri = imageUri,
            allTimeSales = 0,
            modifierList = modifierList?: listOf(),
            lastUpdated = Date(),
            createdAt = createdDate,
            createdBy = createdBy,
            lastUpdatedBy = lastUpdatedBy
        )
    }

    fun submit(edit: Boolean,account: Account) = viewModelScope.launch{
        _addEditFoodResponse.value = UiState.Loading

        var idResult = ProductValidationResult(successful = true)
        if (!edit){
            idResult = validateProductIdUseCase(addEditFoodState.value.productId, ProductType.FoodAndBeverage)
        }
        val nameResult = validateProductNameUseCase(addEditFoodState.value.name)
        val priceResult = validateProductPriceUseCase(addEditFoodState.value.price)
        val categoryResult = validateProductCategoryUseCase(addEditFoodState.value.foodCategory)

        val hasError = listOf(
            idResult,
            nameResult,
            priceResult,
            categoryResult
        ).any{ !it.successful }

        if (hasError){
            _addEditFoodState.value = _addEditFoodState.value.copy(
                productIdError = idResult.errorMessage,
                nameError = nameResult.errorMessage,
                priceError = priceResult.errorMessage,
                foodCategoryError = categoryResult.errorMessage,
            )
            _addEditFoodResponse.value = UiState.Failure(Exception("Field(s) error"))
            return@launch

        } else {
            _addEditFoodState.value = _addEditFoodState.value.copy(
                productIdError = null,
                nameError = null,
                priceError = null,
                foodCategoryError = null,
            )
        }

        if (edit)
            updateFood(account)
        else
            addFood(account)
    }

    private suspend fun addFood(account: Account) {
        val modifierList = if (addEditFoodState.value.isModifiable) addEditFoodState.value.modifierList else null
        addFoodUseCase.invoke(
            account,
            getFood(
                addEditFoodState.value.productId,
                addEditFoodState.value.name,
                addEditFoodState.value.price,
                addEditFoodState.value.description?:"",
                addEditFoodState.value.foodCategory,
                addEditFoodState.value.isModifiable,
                null,
                null,
                modifierList,
                Date(),
                account.id,
                account.id
            ),
            addEditFoodState.value.image
        ){
            _addEditFoodResponse.value = it
        }
    }

    private suspend fun updateFood(account: Account){
        val modifierList = if (addEditFoodState.value.isModifiable) addEditFoodState.value.modifierList else null
        editFoodUseCase.invoke(
            account,
            getFood(
                addEditFoodState.value.productId,
                addEditFoodState.value.name,
                addEditFoodState.value.price,
                addEditFoodState.value.description?:"",
                addEditFoodState.value.foodCategory,
                addEditFoodState.value.isModifiable,
                food.imageUri,
                food.imagePath,
                modifierList,
                food.createdAt?: Date(),
                food.createdBy,
                account.id
            ),
            if (addEditFoodState.value.image.toString() == food.imageUri) null else addEditFoodState.value.image
        ){
            _addEditFoodResponse.value = it
        }
    }


    fun getModifier(id: String): Modifier? {
        return modifierMap[id]
    }

    private fun getModifierList() {
        _modifiers.value = UiState.Loading
        getModifierListUseCase {
            viewModelScope.launch {
                it.collect { result ->
                    if (result is UiState.Success){
                        _modifierMap.clear()
                        for (i in result.data){
                            _modifierMap[i.productId] = i
                        }
                    }
                    _modifiers.value = result
                }
            }
        }
    }


    private fun loadState(){
        onEvent(AddEditFoodEvent.ProductIdChanged(food.productId))
        onEvent(AddEditFoodEvent.NameChanged(food.name))
        onEvent(AddEditFoodEvent.PriceChanged(food.price.toString()))
        onEvent(AddEditFoodEvent.DescriptionChanged(food.description))
        onEvent(AddEditFoodEvent.FoodCategoryChanged(food.category))
        onEvent(AddEditFoodEvent.ModifiableChanged(food.modifiable))
        onEvent(AddEditFoodEvent.ModifierChanged(food.modifierList))
        food.imageUri?.toUri()?.let { AddEditFoodEvent.ImageChanged(it) }?.let { onEvent(it) }
    }

}