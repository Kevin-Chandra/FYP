package com.example.fyp.menucreator.ui.viewmodel

import android.view.View
import androidx.lifecycle.*
import com.example.fyp.database.ProductDatabase
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.data.repository.ModifierItemRepository
import com.example.fyp.menucreator.data.repository.ModifierRepository
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AddEditModifierViewModel @Inject constructor(
    private val modifierRepository: ModifierRepository,
    private val modifierItemRepository: ModifierItemRepository
) : ViewModel() {
    private var productId : String? = null
    private var _modifier : Modifier? = null
    private val deletedItem = TreeSet<String>()

    val modifier : Modifier
        get() = _modifier!!

    val menu = ProductDatabase



    private val _addResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val addResponse = _addResponse.asStateFlow()

    private val _addItemResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val addItemResponse = _addItemResponse.asStateFlow()

    private val _addItemFinishResponse = MutableStateFlow<UiState<Boolean>>(UiState.Loading)
    val addItemFinishResponse = _addItemFinishResponse.asStateFlow()

    private val deleteCache = arrayListOf<String>()



//    private val _addResponse = _addModifierItemResponse.combine(_addModifierResponse){ a,b ->
//        a to b
//    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())
////        combineState(_addModifierItemResponse,_addModifierResponse,viewModelScope)
////        merge(_addModifierResponse,_addModifierItemResponse)
////        .flatMapMerge {}
////        MutableStateFlow<UiState<Boolean>>(UiState.Loading)
//    val addResponse = _addResponse

    private val itemList = arrayListOf<String>()

    private val itemMap =  mutableMapOf<String,ModifierItem>()

    private fun getModifier(
        productId: String,
        name: String,
        isMultipleChoice: Boolean,
        isRequired: Boolean,
        itemList: ArrayList<String>,
    ) : Modifier{
        return Modifier(productId, name, isMultipleChoice, isRequired,itemList)
    }

    private fun getModifierItem(
        productId: String,
        name: String,
        price: String,
    ) : ModifierItem{
        return ModifierItem(productId, name,price.toDoubleOrNull()?:-1.0)
    }

    //Insert modifier sequence
    // addItems -> addNewModifier ->  InsertItems -> insertModifier

    private fun insertModifier(modifier: Modifier){
        _addResponse.value = modifierRepository.addModifier(modifier)
    }

    private fun insertModifierItem(item: ModifierItem){
        _addItemResponse.value = modifierItemRepository.addModifierItem(item)
    }

    fun addNewModifier(id: String,name: String,isMultipleChoice: Boolean,isRequired: Boolean) = viewModelScope.launch{
        _addResponse.value = UiState.Loading
        _addResponse.value = isModifierEntryValid(id,name,isMultipleChoice,false)

        if (addResponse.value is UiState.Success){
            for (i in itemMap){
                insertModifierItem(i.value)
            }
            insertModifier(getModifier(id,name, isMultipleChoice, isRequired, ArrayList(itemMap.keys)))
        }
    }

    fun addItems(id:String, name: String,price: String) = viewModelScope.launch{
        _addItemResponse.value = UiState.Loading
        _addItemResponse.value = isModifierItemEntryValid(id,name,price,false)
        if (addItemResponse.value is UiState.Success){
            itemMap[id] = getModifierItem(id,name,price)
        }
    }

    private suspend fun isModifierEntryValid(productId: String, name: String, multipleChoice: Boolean, isEdit: Boolean): UiState<Boolean> {
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
        } catch (e: Exception) {
            if (e is CancellationException)
                throw e
            else {
                UiState.Failure(e)
            }

        }
    }

    private suspend fun isModifierItemEntryValid(productId: String, name: String, price: String, isEdit: Boolean): UiState<Boolean> {
        return try {
//            throw (java.lang.NumberFormatException("awww"))
            println("Enter the item checker")
            if (!isEdit) {
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


    fun createNewItemList(){
        itemMap.clear()
    }

    fun deleteCache() = viewModelScope.launch{
        for (id in deleteCache){
            modifierItemRepository.deleteModifierItem(id)
        }
//        deleteCache.clear()
    }

    fun error(e : Exception){
        _addItemResponse.value = UiState.Failure(e)
    }




    fun saveModifier() {
//        for(i in itemList)
//            _modifier!!.addItem(i)
        _modifier?.let { ProductDatabase.insertModifier(it) }
    }

    fun updateModifier(){
        deleteModifier()
        saveModifier()
    }

    fun deleteModifier(){
        productId?.let { ProductDatabase.deleteModifier(it) }
    }

    fun setModifier(modifierId: String): Boolean{
        _modifier = ProductDatabase.getModifier(modifierId)
        productId = modifierId
//        _modifier?.let { itemList.addAll(it.modifierList) }
        return (_modifier != null)
    }

    fun addModifierItem(productId: String,name: String,price: Double){
//        val mi = ModifierItem(productId, name, price)
//        ProductDatabase.insertModifierItem(mi)
//        itemList.add(mi.productId)
    }

    fun addModifierItemId(productId: String){
        itemList.add(productId)
    }

    fun deleteModifierItem(id: String?){
        itemList.remove(id)
        if (id != null) {
            deletedItem.add(id)
        }
    }

    fun getModifierItem(modifierItemId: String) : ModifierItem?{
        return ProductDatabase.getModifierItem(modifierItemId)
    }

    fun reset() {
        productId = null
        _modifier = null
        itemList.clear()
        deletedItem.clear()
    }

    fun removeDeletedItemFromDatabase(){
        for (i in deletedItem)
            ProductDatabase.deleteModifierItem(i)
    }

    fun updateModifierItem(id: String, name: String, price: Double) {
        if (itemList.contains(id))
            ProductDatabase.deleteModifierItem(id)
        addModifierItem(id,name,price)
    }


//    private fun observeAddItemModifier() = viewModelScope.launch {
//            addItemResponse.collect() { it ->
//                when (it) {
//                    is UiState.Loading -> {
//                        println("Add item response loading")
//                        binding.progressBar.visibility = View.VISIBLE
//                    }
//                    is UiState.Failure -> {
//                        println("Add item response error")
//                        binding.progressBar.visibility = View.GONE
//                        viewModel.deleteCache()
//                        it.e?.message?.let { it1 -> errorDialog(it1) }
//                    }
//                    is UiState.Success -> {
//                        binding.progressBar.visibility = View.GONE
//                        successToast("Modifier Item Added successfully")
//                        addModifierToVm()
//                    }
//
//                }
//            }
//        }
//    }

}