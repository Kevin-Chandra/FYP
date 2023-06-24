package com.example.fyp.ordering_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.domain.modifier.GetModifierListUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemByReturnUseCase
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.domain.local_database.DeleteItemFromCartUseCase
import com.example.fyp.ordering_system.domain.local_database.GetOrderItemByIdUseCase
import com.example.fyp.ordering_system.domain.local_database.UpsertToCartUseCase
import com.example.fyp.ordering_system.domain.validation.OrderValidationResult
import com.example.fyp.ordering_system.domain.validation.ValidateModifierUseCase
import com.example.fyp.ordering_system.ui.state.AddToCartUiState
import com.example.fyp.ordering_system.util.AddToCartEvent
import com.example.fyp.ordering_system.util.AddToCartState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddToCartViewModel @Inject constructor(
    private val upsertToCartUseCase: UpsertToCartUseCase,
    private val deleteItemFromCartUseCase: DeleteItemFromCartUseCase,
    private val getOrderItemByIdUseCase: GetOrderItemByIdUseCase,
    private val getModifierItemByReturnUseCase: GetModifierItemByReturnUseCase,
    private val getModifierListUseCase: GetModifierListUseCase,
    private val validateModifierUseCase: ValidateModifierUseCase,
) : ViewModel(){

    private lateinit var  food : Food

    private var edit : Boolean = false
    private lateinit var item : OrderItem

    private val _addToCartState = MutableStateFlow(AddToCartState())
    val addToCartState = _addToCartState.asStateFlow()

    private val _addToCartUiState = MutableSharedFlow<AddToCartUiState>()
    val addToCartUiState = _addToCartUiState.asSharedFlow()

    private var modifierMap = mapOf<String,Modifier>()

    fun initializeItemEdit(id : String) = viewModelScope.launch{
        if (!edit){
            edit = true
            item = getOrderItemByIdUseCase(id)
            loadItemToState()
        }
    }

    fun init(list: List<Modifier>){
        modifierMap = list.associateBy { it.productId }

        _addToCartState.update {
            it.copy(
                modifierList = modifierMap.values.associateWith { null }.toMutableMap()
            )
        }
        _addToCartState.update {
            it.copy(
                errorList = modifierMap.keys.associateWith { null }.toMutableMap()
            )
        }
    }

    private fun loadItemToState() = viewModelScope.launch {
        _addToCartUiState.emit(AddToCartUiState(loading = true))

        val map = mutableMapOf<Modifier,MutableList<ModifierItem>>()
        val list = mutableListOf<MutableList<Deferred<ModifierItem?>>>()

        item.modifierItems?.forEach {
            val internalList = mutableListOf<Deferred<ModifierItem?>>()
            it.value.forEach { it1 ->
                internalList.add(async { getModifierItemByReturnUseCase(it1) })
            }
            list.add(internalList)
        }

        onEvent(AddToCartEvent.QuantityChanged(item.quantity))
        item.note?.let { AddToCartEvent.NoteChanged(it) }?.let { onEvent(it) }

        val modifierMap = mutableMapOf<String,Modifier>()
        getModifierListUseCase.invoke {
            it.onEach { state ->
                if (state is UiState.Success){
                    state.data.associateByTo(modifierMap) { modifier -> modifier.productId }
                }
            }.launchIn(viewModelScope)
        }
        list.forEach {
            it.awaitAll().forEach { it1->
                it1 ?: return@forEach
                val modifierParent = modifierMap[it1.modifierParent]?: return@forEach
                if (map[modifierParent] == null){
                    map[modifierParent] = mutableListOf(it1)
                } else {
                    map[modifierParent]?.add(it1)
                }
            }
        }

        map.forEach{
            onEvent(AddToCartEvent.ModifierItemListChanged(it.key,it.value.toList()))
        }
        _addToCartUiState.emit(AddToCartUiState(loading = false))
    }

    fun onEvent(event: AddToCartEvent){
        when(event){
            is AddToCartEvent.FoodChanged -> {
                if (!this::food.isInitialized) {
                    food = event.food
                    if (food.availability) {
                        _addToCartState.update { it.copy(foodId = event.food.productId) }
                        updatePrice()
                    }
                }
            }
            is AddToCartEvent.ModifierItemListChanged -> {
                _addToCartState.value.modifierList[event.modifier] = event.list
                updatePrice()
            }
            is AddToCartEvent.QuantityChanged -> {
                _addToCartState.update { it.copy( quantity = event.qty) }
                updatePrice()
            }
            is AddToCartEvent.NoteChanged -> {
                _addToCartState.update { it.copy( note = event.note) }
            }
            AddToCartEvent.DeleteFromCart -> {
                deleteOrderItem()
            }
            AddToCartEvent.AddToCart -> {
                addToCart()
            }
        }
    }

    private fun deleteOrderItem() = viewModelScope.launch {
        if (edit){
            deleteItemFromCartUseCase(item.orderItemId)
            _addToCartUiState.emit(AddToCartUiState(successAdding = true, loading = false))
        }
    }

    private fun updatePrice() {
        var price = food.price
        _addToCartState.value.modifierList.forEach{ it ->
                it.value?.forEach { it1 ->
                    price += it1.price
                }
        }
        price *= _addToCartState.value.quantity
        _addToCartState.update { _addToCartState.value.copy( price = price) }
    }

    private fun getRandomUUID() = UUID.randomUUID().toString()

    private fun getOrderItem(
        orderItemId: String? = null,
        foodId: String,
        modifierItemList: Map<String,List<String>>?,
        quantity: Int,
        note: String?,
        price: Double
    ) : OrderItem{
        return OrderItem(
            orderItemId = orderItemId ?: getRandomUUID(),
            foodId = foodId,
            modifierItems = modifierItemList,
            quantity = quantity,
            note = note,
            price = price
        )
    }

    private fun addToCart() = viewModelScope.launch{
        _addToCartUiState.emit(AddToCartUiState(loading = true))

        val map = mutableMapOf<String,List<String>>()
        val validationResultList = mutableListOf<OrderValidationResult>()

        _addToCartState.value.modifierList.forEach{
            val result = validateModifierUseCase(it.key,it.value)
            validationResultList.add(result)
            if (!result.successful){
                _addToCartState.value.errorList[it.key.productId] = result.errorMessage
            } else {
                _addToCartState.value.errorList[it.key.productId] = null
            }
        }
        println(validationResultList)
        _addToCartState.update { it }

        if (validationResultList.any { !it.successful }){
            _addToCartUiState.emit (AddToCartUiState(
                errorMessage = "Selection error!",
                loading = false
            ))
            return@launch
        }

        _addToCartState.value.modifierList.forEach {
            if (!it.value.isNullOrEmpty()){
                map[it.key.productId] = it.value?.map { it1 -> it1.productId }!!
            }
        }

        if (edit){
            upsertToCartUseCase(
                getOrderItem(
                    item.orderItemId,
                    _addToCartState.value.foodId,
                    map,
                    _addToCartState.value.quantity,
                    _addToCartState.value.note,
                    _addToCartState.value.price
                ),
                edit
            )
        } else {
            upsertToCartUseCase(
                getOrderItem(
                    null,
                    _addToCartState.value.foodId,
                    map,
                    _addToCartState.value.quantity,
                    _addToCartState.value.note,
                    _addToCartState.value.price
                ),
                edit
            )
        }
        _addToCartUiState.emit(AddToCartUiState(successAdding = true, loading = false))
    }
}