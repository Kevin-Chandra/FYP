package com.example.fyp.pos.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.domain.local_database.DeleteAllOrderItemUseCase
import com.example.fyp.ordering_system.domain.local_database.DeleteItemFromCartUseCase
import com.example.fyp.ordering_system.domain.local_database.GetCartUseCase
import com.example.fyp.ordering_system.domain.local_database.UpsertToCartUseCase
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.domain.table.AddOrderItemToTableUseCase
import com.example.fyp.pos.domain.table.InitializeTableNewOrderUseCase
import com.example.fyp.pos.util.TableOrderEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TableOrderCartViewModel @Inject constructor(
    private val upsertToCartUseCase: UpsertToCartUseCase,
    private val deleteItemFromCartUseCase: DeleteItemFromCartUseCase,
    private val resetDatabaseUseCase: DeleteAllOrderItemUseCase,
    getCartUseCase: GetCartUseCase,
    private val initializeTableNewOrderUseCase: InitializeTableNewOrderUseCase,
    private val addOrderItemToTableUseCase: AddOrderItemToTableUseCase,
) : ViewModel() {

    private val _posOrderUiState = MutableStateFlow<Response<String>>(Response.Success(""))
    val posOrderUiState = _posOrderUiState.asStateFlow()

    lateinit var table: Table
    lateinit var orderId: String

    val cart = mutableStateOf(listOf<OrderItem>())

    init {
        viewModelScope.launch {
            resetDatabaseUseCase()
        }
        getCartUseCase().onEach{
            cart.value = it
        }.launchIn(viewModelScope)
    }

    fun initTable(table: Table){
        orderId = if (table.currentOrder.isNullOrEmpty()){
            getRandomUUID()
        } else {
            table.currentOrder
        }
        this.table = table
    }

    fun onEvent(event: TableOrderEvent){
        when(event){
            is TableOrderEvent.OnDeleteOrderItem -> {
                deleteItem(event.id)
            }
            TableOrderEvent.SubmitOrder -> {
                submitOrder()
            }
            is TableOrderEvent.OnAddFood -> {
                upsertToCart(event.food, event.quantity)
            }
        }
    }

    private fun updateQuantity(orderItem: OrderItem,qty:Int) = viewModelScope.launch{
        if (qty > 0){
            upsertToCartUseCase(orderItem.copy(quantity = qty))
        }
    }

    private fun upsertToCart(food: Food, qty:Int) = viewModelScope.launch{
        upsertToCartUseCase(getOrderItem(food,qty))
    }

    private fun deleteItem(id: String) = viewModelScope.launch {
        deleteItemFromCartUseCase(id)
    }

    private fun submitOrder() = viewModelScope.launch {
        _posOrderUiState.value = Response.Loading
        val job = async {
            if (table.currentOrder.isNullOrEmpty()) {
                table = table.copy(currentOrder = orderId)
                initializeTableNewOrderUseCase(order = Order(orderId = orderId), table = table) {
                }
            }
        }
        job.await()
        addOrderItemToTableUseCase(itemList = cart.value,table){ res ->
            println(res)
            _posOrderUiState.update { res }
            if (res is Response.Success){
                viewModelScope.launch {
                    resetDatabaseUseCase()
                }
            }
        }
    }

    private fun getOrderItem(food: Food, qty: Int) : OrderItem = OrderItem(
        orderId = orderId,
        quantity = qty,
        foodId = food.productId,
        price = food.price * qty,
        orderItemId = getRandomUUID()
    )

    private fun getRandomUUID() = UUID.randomUUID().toString()

    fun resetState(){
        _posOrderUiState.update { Response.Success("") }
    }

}