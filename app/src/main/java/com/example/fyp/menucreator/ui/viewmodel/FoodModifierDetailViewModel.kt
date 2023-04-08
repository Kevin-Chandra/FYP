package com.example.fyp.menucreator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.menucreator.data.model.*
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.domain.food.DeleteFoodUseCase
import com.example.fyp.menucreator.domain.food.GetFoodUseCase
import com.example.fyp.menucreator.domain.food.UpdateFoodUseCase
import com.example.fyp.menucreator.domain.modifier.DeleteModifierUseCase
import com.example.fyp.menucreator.domain.modifier.GetModifierUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemUseCase
import com.example.fyp.menucreator.domain.productImage.DeleteImageUseCase
import com.example.fyp.menucreator.util.MenuCreatorResponse
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodModifierDetailViewModel @Inject constructor(
    private val deleteFoodUseCase: DeleteFoodUseCase,
    private val updateFoodUseCase: UpdateFoodUseCase,
    private val deleteModifierUseCase: DeleteModifierUseCase
) : ViewModel() {

    private val _deleteResponse = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
    val deleteResponse = _deleteResponse.asStateFlow()

    private var _productId : String? = null
    val productId: String get() = _productId!!

    private var _type : ProductType? = null
    val type: ProductType? get() = _type

    fun initialize(id: String, type: ProductType){
        _productId = id
        _type = type
    }
    fun deleteProduct(id: String) = viewModelScope.launch{
        _deleteResponse.value = UiState.Loading
        if (type == ProductType.FoodAndBeverage){
            deleteFoodUseCase(id){
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
            deleteModifierUseCase(id) {
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

    fun removeModifierFromFoodAndUpdate(food : Food, list: List<String>) = viewModelScope.launch{
        val newFood = food.copy(modifierList = list)
        updateFoodUseCase(newFood,null){
            //TODO add response?
        }
    }

}