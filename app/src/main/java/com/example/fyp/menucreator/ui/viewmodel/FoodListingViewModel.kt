package com.example.fyp.menucreator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.domain.food.GetFoodListUseCase
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodListingViewModel @Inject constructor(
    private val getFoodListUseCase: GetFoodListUseCase
) : ViewModel(){

    private val _foods = MutableStateFlow<UiState<List<Food>>>(UiState.Loading)
    val foods = _foods.asStateFlow()

    private val foodMap = mutableMapOf<String,Food>()
    init{
        getFoodList()
    }

    private fun getFoodList() {
        _foods.value = UiState.Loading
        getFoodListUseCase.invoke {
            viewModelScope.launch {
                it.collect {
                    when (it) {
                        is UiState.Success -> {
                            foodMap.clear()
                            for (food in it.data){
                                foodMap[food.productId] = food
                            }
                            _foods.value = UiState.Success(it.data.sortedBy { it1 -> it1.category })
                        }
                        else -> {
                            _foods.value = it
                        }
                    }
                }
            }
        }
    }

    fun getFood(id:String) : Food? {
        return foodMap[id]
    }
}