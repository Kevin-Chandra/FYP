package com.example.fyp.menucreator.ui.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.data.repository.ProductImageRepository
import com.example.fyp.menucreator.domain.DeleteImageUseCase
import com.example.fyp.menucreator.domain.GetImageUseCase
import com.example.fyp.menucreator.domain.UploadImageUseCase
import com.example.fyp.menucreator.util.UiState
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
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
    private val modifierRepository: ModifierRepository,
    private val uploadImageUseCase: UploadImageUseCase,
    private val deleteImageUseCase: DeleteImageUseCase
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

    private val _imageState = MutableStateFlow<UiState<Pair<String,String>>>(UiState.Success(Pair("","")))
    val imageState = _imageState.asStateFlow()

    private val _loadImageState = MutableStateFlow<UiState<Uri?>>(UiState.Success(null))
    val loadImageState = _loadImageState.asStateFlow()

    private var _modifierMap: MutableMap<String, Modifier> = mutableMapOf()
    val modifierMap: Map<String, Modifier> get() = _modifierMap

    private val _foodLoaded = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val foodLoaded = _foodLoaded.asStateFlow()

    private val _updateFoodResponse = MutableSharedFlow<UiState<Boolean>>()
    val updateFoodResponse = _updateFoodResponse.asSharedFlow()

    private val _addFoodResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val addFoodResponse = _addFoodResponse.asStateFlow()

    init {
        getModifierList()
        getFoodList()
        loadModifierMap()
        loadFoodMap()
    }
    fun initialize(id: String) {
        _foodLoaded.value = UiState.Loading
        productId = id
        populate()
    }

    private fun populate() {
        if (_foodMap.isNotEmpty()) {
            _food = foodMap[productId]
//            getImage()
            _foodLoaded.value = UiState.Success(true)
        }
    }

//    private fun getImage(thisFood: Food)  = viewModelScope.launch{
//        _loadImageState.value = UiState.Loading
////        getImageUseCase(food.image)
//        food.image?.let { _loadImageState.value = UiState.Success(getImageUseCase(it)) }
//    }

    private fun insertFood(food: Food){
        _addFoodResponse.value = foodRepository.addFood(food)
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
        modifierList: ArrayList<String>,
        createdDate: Date = Date()
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
            modifierList = modifierList,
            lastUpdated = Date(),
            createdAt = createdDate
        )
    }

    fun addNewFood(
        productId: String,
        name: String,
        price: String,
        description: String,
        category: String,
        isModifiable: Boolean,
        image: Uri?,
        modifierList: ArrayList<String>,
        isImageChanged: Boolean
    )  = viewModelScope.launch (Dispatchers.IO) {
        _addFoodResponse.value = isEntryValid(productId, name, price,category, false)
        if (addFoodResponse.value is UiState.Success) {
            println(image.toString())
            if (isImageChanged) {
                if (image != null){
                    val result = async {uploadImage(image)}.await()
                    insertFood(getFood(productId, name, price, description,category, isModifiable,result.first,result.second, modifierList))
                }
            } else {
                insertFood(getFood(productId, name, price, description,category, isModifiable,null,null, modifierList))
            }
        }
    }

    fun updateFood(
        productId: String,
        name: String,
        price: String,
        description: String,
        category: String,
        image: Uri?,
        isModifiable: Boolean,
        modifierList: ArrayList<String>,
        isImageChanged: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        _updateFoodResponse.emit(UiState.Loading)
        val response = isEntryValid(productId, name, price,category, true)
        _updateFoodResponse.emit(response)
        println("Im stuck1")
        if (response is UiState.Success){
            if (isImageChanged){
                if (image != null){
                    launch { food.imagePath?.let { deleteImage(it) } }
                    val result = async {uploadImage(image)}.await()
                    println("Im stuck")
                    updateFood(getFood(productId, name, price, description,category, isModifiable,result.first,result.second, modifierList,food.createdAt?:Date()),productId)
                    println("passed")
                } else{
                    updateFood(getFood(productId, name, price, description,category, isModifiable,null,null, modifierList,food.createdAt?:Date()),productId)
                }
            } else {
                println("bypass")
                updateFood(getFood(productId, name, price, description,category, isModifiable,food.imageUri,food.imagePath, modifierList,food.createdAt?:Date()),productId)
            }
        }
    }

    private fun updateFood(food: Food, id: String) = viewModelScope.launch(Dispatchers.IO) {
        _updateFoodResponse.emit(foodRepository.updateFood(id, food))
    }

    private suspend fun uploadImage(uri: Uri) : Pair<String,String>{
        var a = Pair("","")
        uploadImageUseCase(uri){
            when (it){
                is UiState.Success -> {
                    _imageState.value = UiState.Success(it.data)
                    a = Pair(it.data.first,it.data.second)
                }
                is UiState.Failure -> {
                    _imageState.value = UiState.Failure(it.e)
                }
                else ->{}
            }
        }
        return a
    }

    private fun deleteImage(path: String){
        deleteImageUseCase(path){
        }
    }

    private suspend fun isEntryValid(productId: String, name: String, price: String,category: String, edit: Boolean): UiState<Boolean> {
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
            if (category.isBlank())
                throw Exception("Category not selected!")
            UiState.Success(false)
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