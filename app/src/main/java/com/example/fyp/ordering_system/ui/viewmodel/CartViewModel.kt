package com.example.fyp.ordering_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.domain.DeleteItemFromCartUseCase
import com.example.fyp.ordering_system.domain.GetCartUseCase
import com.example.fyp.ordering_system.domain.UpsertToCartUseCase
import com.example.fyp.ordering_system.util.OrderingEvent
import com.example.fyp.ordering_system.util.OrderingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val upsertToCartUseCase: UpsertToCartUseCase,
    private val deleteItemFromCartUseCase: DeleteItemFromCartUseCase,
    private val getCartUseCase: GetCartUseCase,
) : ViewModel(){

    private val _orderingState = MutableStateFlow(OrderingState())
    val orderingState = _orderingState.asStateFlow()

    fun onOrderingEvent(event: OrderingEvent){
        when(event){
            OrderingEvent.AddToCart -> {
                addToCart()
            }
            is OrderingEvent.FoodIdChanged -> {
                _orderingState.update { _orderingState.value.copy( foodId = event.id) }
            }
            is OrderingEvent.ModifierItemListChanged -> {
                _orderingState.update { _orderingState.value.copy( modifierList = event.list) }
            }
            is OrderingEvent.QuantityChanged -> {
                _orderingState.update { _orderingState.value.copy( quantity = event.qty) }
            }
        }
    }

    fun getRandomUUID() = UUID.randomUUID().toString()

    fun getOrderItem(
        foodId: String,
        modifierItemList: List<String>?,
        quantity: Int,
        note: String?,
    ) : OrderItem{
        return OrderItem(
            orderItemId = getRandomUUID(),
            foodId = foodId,
            modifierItems = modifierItemList,
            quantity = quantity,
            note = note,
        )
    }
    fun addToCart(){
        upsertToCartUseCase()
    }

}