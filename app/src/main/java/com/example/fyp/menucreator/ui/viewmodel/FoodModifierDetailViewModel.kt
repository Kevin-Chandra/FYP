package com.example.fyp.menucreator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.model.ProductType
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodModifierDetailViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val modifierRepository: ModifierRepository,
    private val itemRepository: ModifierItemRepository
) : ViewModel() {

    private val _foods = MutableStateFlow<UiState<List<Food>>>(UiState.Loading)
    val foods = _foods.asStateFlow()

    private val _modifiers = MutableStateFlow<UiState<Map<String,Modifier>>>(UiState.Loading)
    val modifiers = _modifiers.asStateFlow()

    private val _modifierItems = MutableStateFlow<UiState<Map<String, ModifierItem>>>(UiState.Loading)
    val modifierItems = _modifierItems.asStateFlow()

    private val _productLoaded = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val productLoaded = _productLoaded.asStateFlow()

    private val _foodLoaded = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val foodLoaded = _foodLoaded.asStateFlow()

    private val _modifierLoaded = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val modifierLoaded = _modifierLoaded.asStateFlow()

    private val _deleteResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val deleteResponse = _deleteResponse.asStateFlow()

    private val _deleteItemResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val deleteItemResponse = _deleteItemResponse.asStateFlow()

    private var _foodMap : MutableMap<String,Food> = mutableMapOf()
    val foodMap : Map<String,Food> get() = _foodMap

    private var _modifierMap : MutableMap<String,Modifier> = mutableMapOf()
    val modifierMap : Map<String,Modifier> get() = _modifierMap

    private var _modifierItemMap : MutableMap<String,ModifierItem> = mutableMapOf()
    val modifierItemMap : Map<String,ModifierItem> get() = _modifierItemMap

    private var _productId : String? = null
    val productId: String get() = _productId!!

    private var _type : ProductType? = null
    val type: ProductType? get() = _type

    private var _food : Food? = null
    val food : Food get() = _food!!

    private var _modifier: Modifier? = null
    val modifier get() = _modifier!!

    init{
        _foodLoaded.value = UiState.Loading
        getModifierItemList()
        getModifierList()
        getFoodList()
        loadFoodMap()
        loadModifierMap()
        loadModifierItemMap()
    }
    fun initialize(id: String, type: ProductType){
//        reset()
        println("Initialize vm called $id")
        _productId = id
        _type = type
    }

    private fun populate() = viewModelScope.launch {
        productLoaded.collect() {
            when (it) {
                is UiState.Success -> {
                        if (type == ProductType.FoodAndBeverage) {
                            println("food =  ${getFood(productId)}")
                            _food = getFood(productId)
                            _foodLoaded.value = UiState.Success(true)
                        } else{
                            _modifier = getModifier(productId)
                            _modifierLoaded.value = UiState.Success(true)
                        }

                    }
                else -> _foodLoaded.value = UiState.Loading
            }
        }
    }

    fun getFood(id: String): Food?{
        return foodMap[id]
    }

    fun getModifier(id: String) : Modifier?{
        return modifierMap[id]
    }

    fun getModifierItem(id: String): ModifierItem? {
        return modifierItemMap[id]
    }

    fun deleteProduct(id: String) = viewModelScope.launch{
        _deleteResponse.value = UiState.Loading
        if (type == ProductType.FoodAndBeverage){
            _deleteResponse.value = foodRepository.deleteFood(id)
        } else{
            println("Here???")
            //delete all items before deleting modifier
            for (item in modifier.modifierItemList){
                _deleteItemResponse.value = itemRepository.deleteModifierItem(item)
            }
            _deleteResponse.value = UiState.Loading
            _deleteResponse.value = modifierRepository.deleteModifier(id)
        }
    }

    //retrieve from firestore
    private fun getFoodList() = viewModelScope.launch{
        _foods.value = UiState.Loading
        foodRepository.subscribeFoodUpdates().collect { result ->
            _foods.value = result
        }
    }

    //retrieve from firestore
    private fun getModifierList() = viewModelScope.launch{
        _modifiers.value = UiState.Loading
        modifierRepository.subscribeModifierUpdates().collect { result ->
            _modifiers.value = result
        }
    }

    //retrieve from firestore
    private fun getModifierItemList() = viewModelScope.launch{
        _modifierItems.value = UiState.Loading
        itemRepository.subscribeModifierItemUpdates().collect { result ->
            _modifierItems.value = result
        }
    }

    private fun loadModifierMap() = viewModelScope.launch {
        modifiers.collect() {
            when (it) {
                is UiState.Success -> {
                    _modifierMap = it.data.toMutableMap()
                    println("Modifier Loaded in vm")
                }
                is UiState.Failure -> println(it.e)
                is UiState.Loading -> println("Loading in vm")
            }
        }
    }

    private fun loadModifierItemMap() = viewModelScope.launch {
        modifierItems.collect() {
            when (it) {
                is UiState.Success -> {
                    _modifierItemMap = it.data.toMutableMap()
                    println("Modifier Item Loaded in vm")
                }
                is UiState.Failure -> println(it.e)
                is UiState.Loading -> println("Loading in vm")
            }
        }
    }

    private fun loadFoodMap() = viewModelScope.launch {
        foods.collect() {
            when (it) {
                is UiState.Success -> {
                    for (food in it.data){
                        _foodMap[food.productId] = food
                    }
                    _productLoaded.value = UiState.Success(true)
                    populate()
                    println("food Loaded in vm $_foodMap")
                }
                is UiState.Failure -> println(it.e)
                is UiState.Loading -> println("Loading in vm")
            }
        }
    }

    fun reset(){
        _productId = null
        _type = null
        _food = null
        _modifier = null
    }

    fun removeModifierFromFoodAndUpdate(list: ArrayList<String>) = viewModelScope.launch{
        val newFood = food.copy(modifierList = list)
        foodRepository.updateFood(newFood.productId,newFood)
    }


}