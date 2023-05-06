package com.example.fyp.ordering_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.domain.food.GetFoodListUseCase
import com.example.fyp.menucreator.domain.modifier.GetModifierListUseCase
import com.example.fyp.menucreator.domain.modifierItem.GetModifierItemListUseCase
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.domain.GetOrderFromRemoteByStatusUseCase
import com.example.fyp.ordering_system.domain.GetOrderItemFromRemoteByOrderIdUseCase
import com.example.fyp.ordering_system.domain.UpdateOrderItemStatusUseCase
import com.example.fyp.ordering_system.domain.UpdateOrderItemUseCase
import com.example.fyp.ordering_system.domain.UpdateOrderStatusUseCase
import com.example.fyp.ordering_system.util.ManageOrderEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomingOrderViewModel @Inject constructor(
    private val getOrderFromRemoteByStatusUseCase: GetOrderFromRemoteByStatusUseCase,
    private val getOrderItemFromRemoteByOrderIdUseCase: GetOrderItemFromRemoteByOrderIdUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val updateOrderItemStatusUseCase: UpdateOrderItemStatusUseCase,
) : ViewModel() {

    private val _incomingOrder = MutableStateFlow<Response<List<Order>>>(Response.Loading)
    val incomingOrder = _incomingOrder.asStateFlow()

    private val _ongoingOrder = MutableStateFlow<Response<List<Order>>>(Response.Loading)
    val ongoingOrder = _ongoingOrder.asStateFlow()

//    private val _orderItemListResponse = MutableStateFlow<Response<List<OrderItem>>>(Response.Loading)
//    val orderItemListResponse = _orderItemListResponse.asStateFlow()

//    private val _incomingOrderItem = MutableStateFlow<Response<List<OrderItem>>>(Response.Loading)
//    val incomingOrderItem = _incomingOrderItem.asStateFlow()

    init {
        getIncomingOrder()
        getOngoingOrder()
    }

    private val _modifierItems = MutableStateFlow<UiState<List<ModifierItem>>>(UiState.Loading)
    val modifierItems = _modifierItems.asStateFlow()

    fun onEvent(event: ManageOrderEvent){
        when(event){
            is ManageOrderEvent.OnAcceptOrder -> {
                updateStatus(event.orderId,event.list,OrderStatus.Ongoing,OrderItemStatus.Preparing)
            }
            is ManageOrderEvent.OnClickOrder -> {
//                getOrderItemByOrderId(event.orderId)
            }
            is ManageOrderEvent.OnRejectOrder -> {
                updateStatus(event.orderId,event.list,OrderStatus.Rejected,OrderItemStatus.Cancelled)
            }
        }
    }

    private fun getIncomingOrder() = viewModelScope.launch {
        getOrderFromRemoteByStatusUseCase(OrderStatus.Sent) { it ->
            it.onEach { res ->
                _incomingOrder.update { res }
            }.launchIn(viewModelScope)
        }
    }

    private fun getOngoingOrder() = viewModelScope.launch {
        getOrderFromRemoteByStatusUseCase(OrderStatus.Ongoing) { it ->
            it.onEach { res ->
                _ongoingOrder.update { res }
            }.launchIn(viewModelScope)
        }
    }

    fun getIncomingOrderItem() = viewModelScope.launch {
        getOrderFromRemoteByStatusUseCase(OrderStatus.Sent) {
            _incomingOrder.update { it }
        }
    }

    fun getOrderItemByOrderId(orderId: String, result:(Response<List<OrderItem>>) -> Unit) = viewModelScope.launch {
        result.invoke(Response.Loading)
        getOrderItemFromRemoteByOrderIdUseCase(orderId){ it1 ->
            result.invoke(it1)
        }
    }

    private fun updateStatus(orderId: String, list: List<String>, orderStatus: OrderStatus, orderItemStatus: OrderItemStatus) = viewModelScope.launch{
        updateOrderStatusUseCase(orderId,orderStatus){}
        list.forEach { str ->
            updateOrderItemStatusUseCase(str,orderItemStatus){}
        }
    }
}