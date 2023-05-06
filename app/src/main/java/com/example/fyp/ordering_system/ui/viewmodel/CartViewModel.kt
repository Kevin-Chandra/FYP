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
        getCartUseCase().onEach{ it ->
            cart.value = it
            println(it)
            println(it.size)
        }.launchIn(viewModelScope)
    }

    fun onOrderingEvent(event: OrderingEvent){
        println(event)
        when(event){
//            AddToCartEvent.AddToCart -> {
//                addToCart()
//                resetState()
//            }
//            is AddToCartEvent.FoodChanged -> {
//                food = event.food
//                _orderingState.update { _orderingState.value.copy( foodId = event.food.productId) }
//                updatePrice()
//            }
//            is AddToCartEvent.ModifierItemListChanged -> {
//
//                _orderingState.update { _orderingState.value.copy( modifierList = event.list) }
//                updatePrice()
//            }
//            is AddToCartEvent.QuantityChanged -> {
//                _orderingState.update { _orderingState.value.copy( quantity = event.qty) }
//                updatePrice()
//            }
//            is AddToCartEvent.NoteChanged -> {
//                _orderingState.update { _orderingState.value.copy( note = event.note) }
//            }
            is OrderingEvent.FoodDeletedChanged -> {
                deleteFood(event.id)
            }
//            else -> {}
            OrderingEvent.SubmitOrder -> {
                submitOrder()
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

//    private fun getOrderItem(
//        foodId: String,
//        modifierItemList: List<String>?,
//        quantity: Int,
//        note: String?,
//        price: Double
//    ) : OrderItem{
//        return OrderItem(
//            orderItemId = getRandomUUID(),
//            foodId = foodId,
//            modifierItems = modifierItemList,
//            quantity = quantity,
//            note = note,
//            price = price
//        )
//    }

    private fun getOrder(
        orderItemList : List<OrderItem>
    ) = Order(
        orderId = getRandomUUID(),
        orderList = orderItemList.map{ it.orderItemId },
        taxPercentage = tax,
        subTotal = orderItemList.sumOf { it.price },
        grandTotal = orderItemList.sumOf { it.price } * (1+tax)
    )

//    private fun addToCart() = viewModelScope.launch{
//        upsertToCartUseCase(
//            getOrderItem(
//                orderingState.value.foodId,
//                orderingState.value.modifierList.map { it.productId },
////                    .filter { it.value }.map { it.key.productId },
//                orderingState.value.quantity,
//                orderingState.value.note,
//                orderingState.value.price
//            )
//        )
//    }

    private fun submitOrder() = viewModelScope.launch {
        _orderingUiState.value = Response.Loading
        submitOrderUseCase(getOrder(cart.value), cart.value){
            _orderingUiState.value = it
        }
    }

}