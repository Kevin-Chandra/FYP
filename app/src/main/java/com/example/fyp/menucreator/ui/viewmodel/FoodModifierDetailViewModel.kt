package com.example.fyp.menucreator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.menucreator.data.model.*
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.UpdateProductAvailabilityUseCase
import com.example.fyp.menucreator.domain.food.DeleteFoodUseCase
import com.example.fyp.menucreator.domain.food.GetFoodUseCase
import com.example.fyp.menucreator.domain.food.UpdateFoodUseCase
import com.example.fyp.menucreator.domain.modifier.DeleteModifierUseCase
import com.example.fyp.menucreator.domain.modifier.GetModifierUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemListUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemUseCase
import com.example.fyp.menucreator.domain.productImage.DeleteImageUseCase
import com.example.fyp.menucreator.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodModifierDetailViewModel @Inject constructor(
    private val deleteFoodUseCase: DeleteFoodUseCase,
    private val updateFoodUseCase: UpdateFoodUseCase,
    private val deleteModifierUseCase: DeleteModifierUseCase,
    private val getFoodUseCase: GetFoodUseCase,
    private val getModifierUseCase: GetModifierUseCase,
    private val getModifierItemListUseCase: GetModifierItemListUseCase,
    private val updateProductAvailabilityUseCase: UpdateProductAvailabilityUseCase
) : ViewModel() {

    private val _deleteResponse = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
    val deleteResponse = _deleteResponse.asStateFlow()

    private val _updateAvailabilityResponse = MutableStateFlow<UiState<String>>(UiState.Loading)
    val updateAvailabilityResponse = _updateAvailabilityResponse.asStateFlow()

    private val _availabilityState = MutableStateFlow(SetAvailabilityState())
    val availabilityState = _availabilityState.asStateFlow()

    private var _food: Food? = null
    val food get() = _food

    private var modifier: Modifier? = null

    private var itemMap : MutableMap<String,ModifierItem>? = null

    private var _productId : String? = null
    val productId: String get() = _productId!!

    private var _type : ProductType? = null
    val type: ProductType get() = _type!!

    fun initialize(id: String, type: ProductType){
        if (_productId == null){
            _productId = id
            _type = type
            loadProduct()
        }
    }

    private fun loadProduct() {
        _updateAvailabilityResponse.value = UiState.Loading
        viewModelScope.launch {
            if (type == ProductType.FoodAndBeverage) {
                getFoodUseCase(productId) {
                    when (it) {
                        is UiState.Failure -> {
                            _updateAvailabilityResponse.value = it
                        }
                        UiState.Loading -> {}
                        is UiState.Success -> {
                            _food = it.data
                            loadToState()
                            _updateAvailabilityResponse.value = UiState.Success("Loaded")
                        }
                    }
                }
            } else {
                getModifierItemListUseCase.invoke {
                    viewModelScope.launch {
                        it.collect {
                            if (it is UiState.Success) {
                                itemMap = mutableMapOf()
                                it.data.forEach { item ->
                                    itemMap!![item.productId] = item
                                }
                            }
                        }
                    }
                }
                getModifierUseCase(productId) {
                    when (it) {
                        is UiState.Failure -> {
                            _updateAvailabilityResponse.value = it
                        }
                        UiState.Loading -> {}
                        is UiState.Success -> {
                            modifier = it.data
                            loadToState()
                            _updateAvailabilityResponse.value = UiState.Success("Loaded")
                        }
                    }
                }
            }
        }
    }

    fun getModifierItem(id: String) = itemMap?.get(id)

    private fun loadToState() {
        if (type == ProductType.FoodAndBeverage){
            _availabilityState.value = SetAvailabilityState(
                foodAvailability = _food!!.availability
            )
        } else {
            val map = mutableMapOf<String,Boolean>()
            modifier!!.modifierItemList.forEach {
                val item = itemMap?.get(it)
                item?:return@forEach
                map[it] = item.availability
            }
            _availabilityState.value = SetAvailabilityState(
                modifierItemAvailabilityMap = map
            )
        }
    }

    fun onEvent(event: SetAvailabilityEvent) {
        println(event)
        when(event) {
            is SetAvailabilityEvent.FoodAvailabilityChanged -> {
                _availabilityState.value = availabilityState.value.copy(foodAvailability = event.isFoodAvailable)
            }
            is SetAvailabilityEvent.ModifierItemAvailabilityChanged -> {
                _availabilityState.value.modifierItemAvailabilityMap?.set(event.availabilityList.first,event.availabilityList.second)
//                _availabilityState.value = availabilityState.value.copy(modifierItemAvailabilityMap = availabilityState.value.modifierItemAvailabilityMap[event.availabilityList.key] = event.availabilityList.value)
            }
            is SetAvailabilityEvent.Save -> save(event.account)
        }
    }

    private fun save(account: Account){
        _updateAvailabilityResponse.value = UiState.Loading
        if (type == ProductType.FoodAndBeverage){
            updateFoodAvailability(account,availabilityState.value.foodAvailability)
        } else {
            availabilityState.value.modifierItemAvailabilityMap?.let {
                updateModifierItemAvailability(account,it)
            }
        }
    }

    fun deleteProduct(account:Account, id: String) = viewModelScope.launch{
        _deleteResponse.value = UiState.Loading
        if (type == ProductType.FoodAndBeverage){
            deleteFoodUseCase(account,id){
                when (it) {
                    is UiState.Success -> {
                        _deleteResponse.value = UiState.Success(true)
                    }
                    is UiState.Failure -> {
                        _deleteResponse.value = it
                    }
                    UiState.Loading -> {
                        _deleteResponse.value = UiState.Loading
                    }
                }
            }
        } else {
            deleteModifierUseCase(account,id) {
                when (it) {
                    is UiState.Success -> {
                        if (it.data == MenuCreatorResponse.MODIFIER_DELETED)
                            _deleteResponse.value = UiState.Success(true)
                    }
                    is UiState.Failure -> {
                        _deleteResponse.value = it
                    }
                    UiState.Loading -> {
                        _deleteResponse.value = UiState.Loading
                    }
                }
            }
        }
    }

    private fun updateFoodAvailability(account: Account, value: Boolean) = viewModelScope.launch{
        if (value == _food?.availability){
            _updateAvailabilityResponse.value = UiState.Success("Updated Availability!")
            return@launch
        }
        updateProductAvailabilityUseCase.invoke(account,productId, ProductType.FoodAndBeverage,value){
            _updateAvailabilityResponse.value = it
        }
    }

    private fun updateModifierItemAvailability(account: Account, availabilityMap: Map<String,Boolean>) {
        val parentJob = viewModelScope.launch {
            availabilityMap.forEach {
                if (it.value == itemMap?.get(it.key)?.availability)
                    return@forEach
                launch(Dispatchers.IO) {
                    ensureActive()
                    updateProductAvailabilityUseCase.invoke(
                        account,
                        it.key,
                        ProductType.ModifierItem,
                        it.value
                    ) { response ->
                        if (response is UiState.Failure) {
                            _updateAvailabilityResponse.value = UiState.Failure(response.e)
                            this.cancel()
                        }
                    }
                }
            }
        }
        parentJob.invokeOnCompletion {
            it?.let {
                _updateAvailabilityResponse.value = UiState.Failure(it as Exception)
                return@invokeOnCompletion
            }
            _updateAvailabilityResponse.value = UiState.Success("Updated Availability!")
        }
    }

    fun removeModifierFromFoodAndUpdate(account: Account, food : Food, list: List<String>) = viewModelScope.launch{
        val newFood = food.copy(modifierList = list)
        updateFoodUseCase(account, newFood,null){
            //TODO add response?
        }
    }

}