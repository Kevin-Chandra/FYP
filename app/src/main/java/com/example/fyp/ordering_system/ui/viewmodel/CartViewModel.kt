package com.example.fyp.ordering_system.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.domain.productSettings.GetServiceChargeUseCase
import com.example.fyp.menucreator.domain.productSettings.GetTaxUseCase
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderType
import com.example.fyp.ordering_system.domain.local_database.DeleteItemFromCartUseCase
import com.example.fyp.ordering_system.domain.local_database.GetCartUseCase
import com.example.fyp.ordering_system.domain.remote_database.SubmitOrderUseCase
import com.example.fyp.ordering_system.util.OrderingEvent
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
    private val deleteItemFromCartUseCase: DeleteItemFromCartUseCase,
    getCartUseCase: GetCartUseCase,
    private val submitOrderUseCase: SubmitOrderUseCase,
    private val getTaxUseCase: GetTaxUseCase,
    private val getServiceChargeUseCase: GetServiceChargeUseCase,
) : ViewModel(){

    var tax : Double = 0.0
    private var serviceCharge : Double = 0.0

    val cart = mutableStateOf(listOf<OrderItem>())

    var orderId : String = ""
    private set

    private val _orderingUiState = MutableStateFlow<Response<String>>(Response.Success(""))
    val orderingUiState = _orderingUiState.asStateFlow()
    init {
        viewModelScope.launch {
            tax = getTaxUseCase()
            serviceCharge = getServiceChargeUseCase()
        }
        getCartUseCase().onEach{
            cart.value = it
//            compactOrderItem(it){ compacted ->
//                cart.value = compacted
//            }
        }.launchIn(viewModelScope)
        orderId = ""
    }

    fun onOrderingEvent(event: OrderingEvent){
        when(event){
            is OrderingEvent.FoodDeletedChanged -> {
                deleteFood(event.id)
            }
            is OrderingEvent.SubmitOrder -> {
                submitOrder(event.accountId)
            }
        }
    }

    fun resetState(){
        _orderingUiState.update { Response.Success("") }
    }

    fun getSubTotalPrice() : Double{
        var acc = 0.0
        cart.value.forEach {
            acc += it.price
        }
        return acc
    }

    fun getTaxValue(): Double{
        return getSubTotalPrice() * tax
    }

    fun getServiceChargeValue(): Double{
        return getSubTotalPrice() * serviceCharge
    }

    fun getGrandTotal(): Double{
        return getSubTotalPrice() * (1 + tax + serviceCharge)
    }

    private fun deleteFood(id : String) = viewModelScope.launch{
        deleteItemFromCartUseCase(id)
    }

    fun getTaxPercentage() = tax * 100
    fun getServiceChargePercentage() = serviceCharge * 100

    private fun getRandomUUID() = UUID.randomUUID().toString()

    private fun getOrder(
        orderItemList : List<OrderItem>,
        accountId: String,
        orderId: String = getRandomUUID()
    ) = Order(
        orderId = orderId,
        orderList = orderItemList.map{ it.orderItemId },
        taxPercentage = tax,
        serviceChargePercentage = serviceCharge,
        subTotal = orderItemList.sumOf { it.price },
        grandTotal =  String.format("%.2f",getGrandTotal()).toDouble(),
        orderBy = accountId,
        orderType = OrderType.Online,
        paidStatus = true
    )

    private fun submitOrder(accountId: String) = viewModelScope.launch {
        _orderingUiState.value = Response.Loading
        val id = getRandomUUID()
        orderId = id
        submitOrderUseCase(getOrder(cart.value, accountId, id), cart.value){
            _orderingUiState.value = it
        }
    }

}