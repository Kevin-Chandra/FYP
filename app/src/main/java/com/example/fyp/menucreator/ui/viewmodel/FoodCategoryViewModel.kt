package com.example.fyp.menucreator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.domain.AddFoodCategoryUseCase
import com.example.fyp.menucreator.domain.DeleteFoodCategoryUseCase
import com.example.fyp.menucreator.domain.GetFoodCategoryUseCase
import com.example.fyp.menucreator.domain.UpdateFoodCategoryUseCase
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class FoodCategoryViewModel @Inject constructor(
    private val getFoodCategoryUseCase: GetFoodCategoryUseCase,
    private val deleteFoodCategoryUseCase: DeleteFoodCategoryUseCase,
    private val updateFoodCategoryUseCase: UpdateFoodCategoryUseCase,
    private val addFoodCategoryUseCase: AddFoodCategoryUseCase
) : ViewModel(){

    private val _categories = MutableStateFlow<UiState<List<FoodCategory>>>(UiState.Loading)
    val categories = _categories.asSharedFlow()

    private val _deleteState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
    val deleteState = _deleteState.asStateFlow()

    private val _addState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
    val addState = _addState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
    val updateState = _updateState.asStateFlow()

    init {
        getCategoryList()
    }

    private fun getCategoryList() = viewModelScope.launch {
        _categories.value = UiState.Loading
        getFoodCategoryUseCase.invoke().collect{
            _categories.value = it
        }
    }

    private fun deleteFoodCategory(id: String) = viewModelScope.launch {
        deleteFoodCategoryUseCase(id){
            _deleteState.value = it
        }
    }

    private fun updateFoodCategory(category: FoodCategory) = viewModelScope.launch {
        updateFoodCategoryUseCase(category){
            _deleteState.value = it
        }
    }

    private fun addCategory(category: FoodCategory) = viewModelScope.launch {
        addFoodCategoryUseCase(category){
            _addState.value = it
        }
    }

    private fun getCategory(id : String = "", name: String) : FoodCategory{
        return FoodCategory(
            id,
            name
        )
    }

    fun addNewCategory(name: String){
        _addState.value = UiState.Loading
        if (name.isBlank() || name.isEmpty())
            _addState.value = UiState.Failure(Exception("Name is blank!"))
        else
            addCategory(getCategory(name = name))
    }

    fun updateCategory(id : String, name : String){
        _updateState.value = UiState.Loading
        updateFoodCategory(getCategory(id = id, name = name))
    }

    fun deleteCategory(id: String){
        _deleteState.value = UiState.Loading
        deleteFoodCategory(id)
    }

}