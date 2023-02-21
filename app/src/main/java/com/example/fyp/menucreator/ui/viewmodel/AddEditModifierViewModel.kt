package com.example.fyp.menucreator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.database.ProductDatabase
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.util.NavigationCommand
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.arrayListOf
import kotlin.collections.get
import kotlin.collections.iterator
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AddEditModifierViewModel @Inject constructor(
    private val modifierRepository: ModifierRepository,
    private val modifierItemRepository: ModifierItemRepository
) : ViewModel() {

    private var _productId: String? = null
    val productId get() = _productId

    private var _modifier: Modifier? = null
    val modifier get() = _modifier!!

    var itemCount = 0
        private set

    var count = 0
        private set

    var load = 0
        private set

    private val _modifiers = MutableStateFlow<UiState<Map<String, Modifier>>>(UiState.Loading)
    val modifiers = _modifiers.asStateFlow()

    private var _modifierMap: Map<String, Modifier>? = null
    private val modifierMap by lazy { _modifierMap!! }

    private val _items = MutableStateFlow<UiState<Map<String, ModifierItem>>>(UiState.Loading)
    val items = _items.asStateFlow()

    private var _modifierItemMap: Map<String, ModifierItem>? = null
    private val modifierItemMap by lazy { _modifierItemMap!! }

    private val _modifierLoaded = MutableSharedFlow<UiState<Int>>()
    val modifierLoaded = _modifierLoaded.asSharedFlow()

    private val _loadResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val loadResponse = _loadResponse.asStateFlow()

    private val _addResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val addResponse = _addResponse.asStateFlow()

    private val _editResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val editResponse = _editResponse.asStateFlow()

    private val _editItemResponse = MutableStateFlow<UiState<Int>>(UiState.Loading)
    val editItemResponse = _editItemResponse.asStateFlow()

    private val _addItemResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val addItemResponse = _addItemResponse.asStateFlow()

    private val _addItemFinishResponse = MutableStateFlow<UiState<Int>>(UiState.Success(count))
    val addItemFinishResponse = _addItemFinishResponse.asStateFlow()

    private val _editFinishResponse = MutableSharedFlow<UiState<Boolean>>()
    val editFinishResponse = _editFinishResponse.asSharedFlow()

    private val deleteCache = arrayListOf<String>()

    private var isLoadedObserved = false

    private val itemMap = mutableMapOf<String, ModifierItem>()

    init {
        getModifierList()
        getModifierItemList()
        observeGet()
    }

    fun initialize(id: String) {
        if (!isLoadedObserved) {
            isLoadedObserved = !isLoadedObserved
            observeLoaded()
        }
        _productId = id
    }

    private fun populate() {
        _modifier = modifierMap[productId]
    }

    private fun getModifier(
        productId: String,
        name: String,
        isMultipleChoice: Boolean,
        isRequired: Boolean,
        itemList: ArrayList<String>,
    ): Modifier {
        return Modifier(productId, name, isMultipleChoice, isRequired, itemList)
    }

    private fun getModifierItem(
        productId: String,
        name: String,
        price: String,
    ): ModifierItem {
        return ModifierItem(productId, name, price.toDoubleOrNull() ?: -1.0)
    }

    //Insert modifier sequence
    // addItems -> addNewModifier ->  InsertItems -> insertModifier

    private fun insertModifier(modifier: Modifier) {
        _addResponse.value = modifierRepository.addModifier(modifier)
    }

    private fun insertModifierItem(item: ModifierItem) {
        _addItemResponse.value = modifierItemRepository.addModifierItem(item)
    }

    private suspend fun updateModifier(modifier: Modifier) {
        println("item to upload : ${modifier.name}")
        _editFinishResponse.emit(modifierRepository.updateModifier(modifier.productId, modifier))
    }

    private suspend fun updateModifierItem(item: ModifierItem) {
        println("item to upload : ${item.name}")
        _editFinishResponse.emit(modifierItemRepository.updateModifierItem(item.productId,item))
    }

    private suspend fun deleteModifierItemInRepo(id: String){
        modifierItemRepository.deleteModifierItem(id)
    }

    fun addNewModifier(
        id: String,
        name: String,
        isMultipleChoice: Boolean,
        isRequired: Boolean,
        isEdit: Boolean
    ) = viewModelScope.launch(Dispatchers.IO) {
        val response = if (isEdit) _editResponse else _addResponse
        response.value = UiState.Loading
        response.value = isModifierEntryValid(id, name, isMultipleChoice, isEdit)
        if (response.value is UiState.Success) {
            println("everything success waiting to upload")
            val getModifier =
                getModifier(id, name, isMultipleChoice, isRequired, ArrayList(itemMap.keys))
            for (i in deleteCache){
                println("$i to be deleted")
                launch(Dispatchers.IO){
                    deleteModifierItemInRepo(i)
                }
            }
            if (isEdit){
                launch(Dispatchers.IO){
                    for (i in itemMap){
                        launch(Dispatchers.IO) {
                            updateModifierItem(i.value)
                        }
                    }
                }
                updateModifier(getModifier)
            } else {
                launch(Dispatchers.IO) {
                    for (i in itemMap)
                        launch(Dispatchers.IO) {
                            insertModifierItem(i.value)
                        }
                }
                insertModifier(getModifier)
            }
        }
    }


    fun addItems(id: String, name: String, price: String) = viewModelScope.launch {
        _addItemFinishResponse.value = UiState.Loading
        _addItemResponse.value = UiState.Loading
        _addItemResponse.value = isModifierItemEntryValid(id, name, price)
        if (addItemResponse.value is UiState.Success) {
            if (itemMap[id] == null) {
                itemMap[id] = getModifierItem(id, name, price)
                _addItemFinishResponse.update {
                    UiState.Success(count)
                }
            } else {
                _addItemResponse.value = UiState.Failure(Exception("Item ID [$id] already exist!"))
            }
        }
    }

    private suspend fun isModifierEntryValid(
        productId: String,
        name: String,
        multipleChoice: Boolean,
        isEdit: Boolean
    ): UiState<Boolean> {
        return try {
            if (!isEdit) {
                if (productId.isBlank())
                    throw Exception("Modifier ID is blank!")
                if (checkModifierId(productId))
                    throw Exception("Modifier ID already exist!")
            }
            if (name.isBlank())
                throw Exception("Name is blank!")
            if (itemMap.isEmpty())
                throw Exception("Modifier Item must not be empty!")
            if (multipleChoice && itemMap.size < 2)
                throw Exception("Modifier Item must be minimum of 2 for multiple choice")
            UiState.Success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            UiState.Failure(e)
        }
    }

    private fun incrementCount() {
        count++
        println("count = $count")
    }

    private suspend fun isModifierItemEntryValid(
        productId: String,
        name: String,
        price: String,
        isEdit: Boolean = false,
    ): UiState<Boolean> {
        return try {
            if (!isEdit){
                if (productId.isBlank())
                    throw Exception("Item ID is blank!")
                if (checkModifierItemId(productId))
                    throw Exception("[$productId] Item ID already exist!")
            }
            if (name.isBlank())
                throw Exception("[$productId] Name is blank!")
            if (price.isBlank())
                throw Exception("[$productId] Price is blank!")
            if (price.toDouble() < 0.0)
                throw Exception("[$productId] Price cannot be negative!")
            incrementCount()
            UiState.Success(false)
        } catch (e: Exception) {
            if (e is CancellationException)
                throw e
            else {
//                deleteCache.add(productId)
                UiState.Failure(e)
            }
        }
    }

    private suspend fun checkModifierId(id: String) =
        withContext(Dispatchers.IO) {
            async { modifierRepository.checkModifierId(id) }.await()
        }

    private suspend fun checkModifierItemId(id: String) =
        withContext(Dispatchers.IO) {
            async { modifierItemRepository.checkModifierId(id) }.await()
        }


    fun createNewItemList() {
        count = 0
        itemCount = 0
        itemMap.clear()
    }

    fun clearDeleteCache(){
        deleteCache.clear()
    }

    fun getModifierItem(id: String): ModifierItem? {
        return modifierItemMap[id]
    }

    private fun observeGet() {
        _loadResponse.value = UiState.Loading
        modifiers.onEach {
            if (it is UiState.Success) {
                _modifierMap = it.data
                println("modifierMap load finish")
                load = if (load < 2) load.inc() else 2
                _modifierLoaded.emit(UiState.Success(load))
            }
        }.launchIn(viewModelScope)

        items.onEach {
            if (it is UiState.Success) {
                _modifierItemMap = it.data
                println("modifierItemMap load finish")
                load = if (load < 2) load.inc() else 2
                _modifierLoaded.emit(UiState.Success(load))
            }
        }.launchIn(viewModelScope)

    }
    private fun observeLoaded() =
        modifierLoaded.onEach {
            if (it is UiState.Success && it.data >= 2) {
                populate()
                _loadResponse.value = UiState.Success(true)
            }
        }.launchIn(viewModelScope)

    private fun getModifierList() = viewModelScope.launch{
        _modifiers.value = UiState.Loading
        modifierRepository.subscribeModifierUpdates().collect { result ->
            _modifiers.value = result
        }
    }
    private fun getModifierItemList() = viewModelScope.launch{
        _items.value = UiState.Loading
        modifierItemRepository.subscribeModifierItemUpdates().collect { result ->
            _items.value = result
        }
    }

    fun deleteModifierItem(id: String) {
        itemMap.remove(id)
        deleteCache.add(id)
        println("$id added to delete cache")
    }

    fun updateItem(id: String, name: String, price: String, isEdit: Boolean) = viewModelScope.launch{
        _editItemResponse.value = UiState.Loading
        _editResponse.value = isModifierItemEntryValid(id,name, price,isEdit)
        if (_editResponse.value is UiState.Success){
            if (itemMap[id] == null) {
                itemMap[id] = getModifierItem(id, name, price)
                itemCount += 1
                _editItemResponse.update {
                    UiState.Success(itemCount)
                }
            } else {
                _editItemResponse.value = UiState.Failure(Exception("Item ID [$id] already exist!"))
            }
        }
    }


}