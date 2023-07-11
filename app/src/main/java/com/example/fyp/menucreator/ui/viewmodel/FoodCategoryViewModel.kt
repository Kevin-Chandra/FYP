package com.example.fyp.menucreator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.domain.foodCategory.AddFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.DeleteFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.GetFoodCategoryUseCase
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val addFoodCategoryUseCase: AddFoodCategoryUseCase
) : ViewModel(){

    private val _categories = MutableStateFlow<UiState<List<FoodCategory>>>(UiState.Loading)
    val categories = _categories.asSharedFlow()

    private val _deleteState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
    val deleteState = _deleteState.asStateFlow()

    private val _addState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
    val addState = _addState.asStateFlow()

    init {
        getCategoryList()
    }

    private fun getCategoryList() = viewModelScope.launch {
        _categories.value = UiState.Loading
        getFoodCategoryUseCase.invoke().collect{
            _categories.value = it
        }
    }

    private fun deleteFoodCategory(account: Account, id: String) = viewModelScope.launch {
        deleteFoodCategoryUseCase(account,id){
            _deleteState.value = it
        }
    }

//    private fun updateFoodCategory(category: FoodCategory) = viewModelScope.launch {
//        updateFoodCategoryUseCase(category){
//            _deleteState.value = it
//        }
//    }

    private fun addCategory(account: Account,category: FoodCategory) = viewModelScope.launch {
        addFoodCategoryUseCase(account,category){
            _addState.value = it
        }
    }

    private fun getCategory(id : String = "", name: String) : FoodCategory{
        return FoodCategory(
            id,
            name
        )
    }

    fun addNewCategory(account: Account, name: String){
        _addState.value = UiState.Loading
        if (name.isBlank() || name.isEmpty())
            _addState.value = UiState.Failure(Exception("Name is blank!"))
        else
            addCategory(account,getCategory(name = name))
    }

//    fun updateCategory(id : String, name : String){
//        _updateState.value = UiState.Loading
//        updateFoodCategory(getCategory(id = id, name = name))
//    }

    fun deleteCategory(account:Account, id: String){
        _deleteState.value = UiState.Loading
        deleteFoodCategory(account, id)
    }

}