package com.example.fyp.ordering_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.domain.food.GetFoodListUseCase
import com.example.fyp.menucreator.domain.foodCategory.GetFoodCategoryUseCase
import com.example.fyp.menucreator.domain.modifier.GetModifierListUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemListUseCase
import com.example.fyp.menucreator.domain.productSettings.InsertSettingUseCase
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getFoodListUseCase: GetFoodListUseCase,
    private val getModifierListUseCase: GetModifierListUseCase,
    private val getModifierItemListUseCase: GetModifierItemListUseCase,
    private val insertSettingUseCase: InsertSettingUseCase,
    private val getFoodCategory: GetFoodCategoryUseCase,
) : ViewModel(){

    private val _foodCategories = MutableStateFlow<UiState<List<FoodCategory>>>(UiState.Loading)
    val foodCategories = _foodCategories.asStateFlow()

    private val _allFoods = MutableStateFlow<UiState<List<Food>>>(UiState.Loading)
    val allFoods = _allFoods.asStateFlow()

    private val _filteredFoods = MutableStateFlow<UiState<List<Food>>>(UiState.Loading)
    val filteredFoods = _filteredFoods.asStateFlow()

    private val _modifiers = MutableStateFlow<UiState<List<Modifier>>>(UiState.Loading)
    val modifiers = _modifiers.asStateFlow()

    private val _modifierItems = MutableStateFlow<UiState<List<ModifierItem>>>(UiState.Loading)
    val modifierItems = _modifierItems.asStateFlow()

    private val foodMap = mutableMapOf<String,Food>()
    private val modifierMap = mutableMapOf<String,Modifier>()
    private val itemMap = mutableMapOf<String,ModifierItem>()

    private val _selectedCategory = MutableStateFlow<FoodCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    init {
        viewModelScope.launch {
            insertSettingUseCase.invoke{}
        }
        getItemList()
        getFoodList()
        getModifierList()
        getFoodCategories()
    }

    private fun getFoodList() {
        _allFoods.value = UiState.Loading
        getFoodListUseCase.invoke {
            viewModelScope.launch {
                it.collect {
                    when (it) {
                        is UiState.Success -> {
                            foodMap.clear()
                            for (food in it.data){
                                foodMap[food.productId] = food
                            }
                            _filteredFoods.value = UiState.Success(it.data.sortedBy { it1 -> it1.category })
                            _allFoods.value = UiState.Success(it.data.sortedBy { it1 -> it1.category })
                        }
                        else -> {
                            _allFoods.value = it
                        }
                    }
                }
            }
        }
    }

    private fun getFoodCategories() {
        _foodCategories.value = UiState.Loading
        getFoodCategory().onEach {
            _foodCategories.value = it
        }.launchIn(viewModelScope)
    }

    fun filterFoodByCategory(category: FoodCategory?) = viewModelScope.launch{
        _filteredFoods.value = UiState.Loading
        var filtered = emptyList<Food>()
        if (_allFoods.value is UiState.Success){
            filtered = (_allFoods.value as UiState.Success<List<Food>>).data
            if (category != null){
                _selectedCategory.value = category
                filtered = (_allFoods.value as UiState.Success<List<Food>>).data.filter {
                    it.category == category.name
                }
            } else {
                _selectedCategory.value = null
            }
        }
        _filteredFoods.value = UiState.Success(filtered)
    }

    private fun getModifierList() {
        _modifiers.value = UiState.Loading
        getModifierListUseCase.invoke {
            viewModelScope.launch {
                it.collect {
                    when (it) {
                        is UiState.Success -> {
                            modifierMap.clear()
                            for (modifier in it.data){
                                modifierMap[modifier.productId] = modifier
                            }
                            _modifiers.value = UiState.Success(it.data)
                        }
                        else -> {
                            _modifiers.value = it
                        }
                    }
                }
            }
        }
    }

    private fun getItemList() {
        _modifierItems.value = UiState.Loading
        getModifierItemListUseCase.invoke {
            viewModelScope.launch {
                it.collect {
                    when (it) {
                        is UiState.Success -> {
                            itemMap.clear()
                            for (item in it.data){
                                itemMap[item.productId] = item
                            }
                            _modifierItems.value = UiState.Success(it.data)
                        }
                        else -> {
                            _modifierItems.value = it
                        }
                    }
                }
            }
        }
    }

    fun getModifier(id: String) = modifierMap[id]

    fun getModifierItem(id: String) = itemMap[id]
    fun getFood(id: String) = foodMap[id]
}