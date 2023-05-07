package com.example.fyp.ordering_system.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.Result
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.domain.DeleteItemFromCartUseCase
import com.example.fyp.ordering_system.domain.GetCartUseCase
import com.example.fyp.ordering_system.domain.SubmitOrderUseCase
import com.example.fyp.ordering_system.domain.UpsertToCartUseCase
import com.example.fyp.ordering_system.util.AddToCartEvent
import com.example.fyp.ordering_system.util.AddToCartState
import com.example.fyp.ordering_system.util.OrderingEvent
import com.example.fyp.ordering_system.util.OrderingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val upsertToCartUseCase: UpsertToCartUseCase,
    private val deleteItemFromCartUseCase: DeleteItemFromCartUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val submitOrderUseCase: SubmitOrderUseCase,
) : ViewModel(){

    private val tax = 0.06

    val cart = mutableStateOf(listOf<OrderItem>())

    private val _orderingState = MutableStateFlow(OrderingState())
    val orderingState = _orderingState.asStateFlow()

    private val _orderingUiState = MutableStateFlow<Response<String>>(Response.Success(""))
    val orderingUiState = _orderingUiState.asStateFlow()
    init {
        getCartUseCase().onEach{
            cart.value = it
        }.launchIn(viewModelScope)
    }

    fun onOrderingEvent(event: OrderingEvent){
        println(event)
        when(event){
            is OrderingEvent.FoodDeletedChanged -> {
                deleteFood(event.id)
            }
            is OrderingEvent.SubmitOrder -> {
                submitOrder(event.accountId)
            }
        }
    }

    fun getSubTotalPrice() : Double{
        var acc = 0.0
        cart.value.forEach {
            acc += it.price
        }
        return acc
    }

    private fun deleteFood(id : String) = viewModelScope.launch{
        deleteItemFromCartUseCase(id)
    }

    private fun getRandomUUID() = UUID.randomUUID().toString()

    private fun getOrder(
        orderItemList : List<OrderItem>,
        accountId: String
    ) = Order(
        orderId = getRandomUUID(),
        orderList = orderItemList.map{ it.orderItemId },
        taxPercentage = tax,
        subTotal = orderItemList.sumOf { it.price },
        grandTotal =  DecimalFormat("#.##").format(orderItemList.sumOf { it.price } * (1+tax)).toDouble(),
        orderBy = accountId
    )

    private fun submitOrder(accountId: String) = viewModelScope.launch {
        _orderingUiState.value = Response.Loading
        submitOrderUseCase(getOrder(cart.value, accountId), cart.value){
            _orderingUiState.value = it
        }
    }

}