package com.example.fyp.menucreator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

// This view model assist in view food details, add new food, edit food. Two fragments is under
// this view model which is AddEditFoodFragment and SecondFragment
@HiltViewModel
class AddEditFoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val modifierRepository: ModifierRepository
) : ViewModel() {

    private var productId: String? = null
    private var _food: Food? = null
    val food: Food
        get() = _food!!

    private val _foods = MutableStateFlow<UiState<List<Food>>>(UiState.Loading)
    val foods = _foods.asStateFlow()

    private var _foodMap: MutableMap<String, Food> = mutableMapOf()
    val foodMap: Map<String, Food> get() = _foodMap

    private val _modifiers = MutableStateFlow<UiState<Map<String, Modifier>>>(UiState.Loading)
    val modifiers = _modifiers.asStateFlow()

    private var _modifierMap: MutableMap<String, Modifier> = mutableMapOf()
    val modifierMap: Map<String, Modifier> get() = _modifierMap

    private val _foodLoaded = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val foodLoaded = _foodLoaded.asStateFlow()

    private val _updateFoodResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val updateFoodResponse = _updateFoodResponse.asStateFlow()

    private val _addFoodResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val addFoodResponse = _addFoodResponse.asStateFlow()

    init {
        getModifierList()
        getFoodList()
        loadModifierMap()
        loadFoodMap()
    }
    fun initialize(id: String) {
        println("initialize block")
        _foodLoaded.value = UiState.Loading
        productId = id
        populate()
    }

    private fun populate() {
        if (_foodMap.isNotEmpty()) {
            _food = foodMap[productId]
            _foodLoaded.value = UiState.Success(true)
        }
    }

    private suspend fun insertFood(food: Food): UiState<Boolean> {
        _addFoodResponse.value = foodRepository.addFood(food)
        return addFoodResponse.value
    }

    private fun getFood(
        productId: String,
        name: String,
        price: String,
        description: String,
        isModifiable: Boolean,
        modifierList: ArrayList<String>
    ): Food {
        return Food(
            productId = productId,
            name = name,
            price = price.toDouble(),
            description = description,
            category = null,
            modifiable = isModifiable,
            allTimeSales = 0,
            modifierList = modifierList,
            date = Date()
        )
    }

    fun addNewFood(
        productId: String,
        name: String,
        price: String,
        description: String,
        isModifiable: Boolean,
        modifierList: ArrayList<String>
    )  = viewModelScope.launch (Dispatchers.IO) {

        _addFoodResponse.value = UiState.Loading
        _addFoodResponse.value = isEntryValid(productId, name, price, false)
        if (addFoodResponse.value is UiState.Success) {
            insertFood(getFood(productId, name, price, description, isModifiable, modifierList))
        }
    }

    fun updateFood(
        productId: String,
        name: String,
        price: String,
        description: String,
        isModifiable: Boolean,
        modifierList: ArrayList<String>
    ) = viewModelScope.launch(Dispatchers.IO) {

        _updateFoodResponse.value = UiState.Loading
        _updateFoodResponse.value = isEntryValid(productId, name, price, true)
        if (updateFoodResponse.value is UiState.Success)
            updateFood(getFood(productId, name, price, description, isModifiable, modifierList),productId)

    }

    private fun updateFood(food: Food, id: String) = viewModelScope.launch(Dispatchers.IO) {
        _updateFoodResponse.value = foodRepository.updateFood(id, food)
    }

    private suspend fun isEntryValid(productId: String, name: String, price: String, edit: Boolean): UiState<Boolean> {
        return try {
            if (!edit) {
                if (productId.isBlank())
                    throw Exception("Product ID is blank!")
                if (checkFoodId(productId))
                    throw Exception("Product ID already exist!")
            }
            if (name.isBlank())
                throw Exception("Name is blank!")
            if (price.isBlank())
                throw Exception("Price is blank!")
            if (price.toDouble() < 0.0)
                throw Exception("Price cannot be negative!")
            UiState.Success(true)
        } catch (e: Exception) {
            if (e is CancellationException)
                throw e
            else {
                println("Invalid!! ${e.message}")
                UiState.Failure(e)
            }
        }
    }

    private suspend fun checkFoodId(id: String) =
        withContext(Dispatchers.IO) {
            async { foodRepository.checkFoodId(id) }.await()
        }

//    private fun observeValidator(action: (UiState<Boolean>) -> Unit ){
//        viewModelScope.launch {
//            validator.collect(){
//                when(it){
//                    is UiState.Success -> action.invoke(UiState.Success(true))
//                    is UiState.Failure -> validator.value
//                    else -> {}
//                }
//            }
//        }
//    }


//    fun addModifier(modifier: Modifier){
//        _food?.addModifier(modifier.productId)
//    }
//
//    fun addModifierId(modifierId: String){
//        _food?.addModifier(modifierId)
//    }
//
//    fun removeModifier(modifier: Modifier){
//        _food?.removeModifier(modifier.productId)
//    }
//
//    fun removeModifierId(modifierId: String){
//        _food?.removeModifier(modifierId)
//    }
//
//    fun resetModifierList(){
//        _food?.clearModifierList()
//    }

//    fun saveFood() {
//        _food?.let { menu.insertFood(it) }
//    }
//
//    fun updateFood(){
//        productId?.let { menu.deleteFood(it) }
//        saveFood()
//    }
//
//    fun deleteFood(){
//        productId?.let { menu.deleteFood(it) }
//    }
//
//    fun getModifierKeyListFromDatabase(): List<String> {
//        return menu.getModifierKeyList()
//    }

//    private fun initMap(){
//        viewModelScope.launch {
//            modifiers.collect() {
//                when(it){
//                    is UiState.Success -> _modifierMap = it.data.toMutableMap()
//                    else -> {}
//                }
//            }
//        }
//    }
//
    fun getModifier(id: String): Modifier? {
        return modifierMap[id]
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

    private fun getModifierList() = viewModelScope.launch{
        _modifiers.value = UiState.Loading
        modifierRepository.subscribeModifierUpdates().collect { result ->
            _modifiers.value = result
        }
    }
    //retrieve from firestore
    private fun getFoodList() = viewModelScope.launch{
        _foods.value = UiState.Loading
        foodRepository.subscribeFoodUpdates().collect { result ->
            _foods.value = result
        }
    }

    private fun loadFoodMap() = viewModelScope.launch {
        foods.collect() {
            when (it) {
                is UiState.Success -> {
                    for (food in it.data){
                        _foodMap[food.productId] = food
                    }
                    populate()
                    println("food Loaded in vm $_foodMap")
                }
                is UiState.Failure -> println(it.e)
                is UiState.Loading -> println("Loading in vm")
            }
        }
    }


    fun reset() {
        productId = null
        _food = null
    }


}