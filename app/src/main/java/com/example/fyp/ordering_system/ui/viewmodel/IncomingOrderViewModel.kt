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
import com.example.fyp.ordering_system.ui.state.IncomingOrderUiState
import com.example.fyp.ordering_system.util.ManageOrderEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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

    private val _incomingOrderUiState = MutableStateFlow(IncomingOrderUiState())
    val incomingOrderUiState = _incomingOrderUiState.asStateFlow()

    private val _ongoingOrders = MutableStateFlow<List<Order>>(emptyList())
    val ongoingOrders = _ongoingOrders.asStateFlow()

//    private val _updateOrderStatus = MutableStateFlow<Response<String>>(Response.Success(""))
//    val incomingOrderUiState = _incomingOrderUiState.asStateFlow()
//    private val _orderItemListResponse = MutableStateFlow<Response<List<OrderItem>>>(Response.Loading)
//    val orderItemListResponse = _orderItemListResponse.asStateFlow()

    private val _incomingOrders = MutableStateFlow<List<Order>>(emptyList())
    val incomingOrders = _incomingOrders.asStateFlow()

//    val state = combine(_incomingOrders,_updateOrderStatus){ orders,update ->
//        when(orders){
//            is Response.Error -> TODO()
//            Response.Loading -> {}
//            is Response.Success -> TODO()
//        }
//
//        if (orders is Response.Success){
//            _incomingOrderUiState.value.copy(loading = false,data = orders.data, success = true)
//        } else if (orders is Response.Error){
//
//        } else {
//
//        }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(),null)

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
        _incomingOrderUiState.update { IncomingOrderUiState(loading = true) }
        getOrderFromRemoteByStatusUseCase(OrderStatus.Sent) { it ->
            it.onEach { res ->
                when (res){
                    is Response.Error ->{
                        _incomingOrderUiState.update { _incomingOrderUiState.value.copy(
                            errorMessage = res.exception.message,
                            success = false,
                            loading = false
                        ) }
                    }
                    Response.Loading -> {
                        _incomingOrderUiState.update { _incomingOrderUiState.value.copy(loading = true) }
                    }
                    is Response.Success -> {
                        _incomingOrderUiState.update {
                            _incomingOrderUiState.value.copy(
                                errorMessage = null,
                                success = true,
//                                data = res.data,
                                loading = false
                            )
                        }
                        _incomingOrders.update { res.data }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun getOngoingOrder() = viewModelScope.launch {
        getOrderFromRemoteByStatusUseCase(OrderStatus.Ongoing) { it ->
            it.onEach { res ->
                when (res){
                    is Response.Error ->{
                        _incomingOrderUiState.update { _incomingOrderUiState.value.copy(
                            errorMessage = res.exception.message,
                            success = false,
                            loading = false
                        ) }
                    }
                    Response.Loading -> {
                        _incomingOrderUiState.update { _incomingOrderUiState.value.copy(loading = true) }
                    }
                    is Response.Success -> {
                        _incomingOrderUiState.update {
                            _incomingOrderUiState.value.copy(
                                errorMessage = null,
                                success = true,
                                loading = false
                            )
                        }
                        _ongoingOrders.update { res.data }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

//    fun getIncomingOrderItem() = viewModelScope.launch {
//        getOrderFromRemoteByStatusUseCase(OrderStatus.Sent) {
//            _incomingOrder.update { it }
//        }
//    }

    fun getOrderItemByOrderId(orderId: String, result:(Response<List<OrderItem>>) -> Unit) = viewModelScope.launch {
        result.invoke(Response.Loading)
        getOrderItemFromRemoteByOrderIdUseCase(orderId){ it1 ->
            result.invoke(it1)
        }
    }

    private fun updateStatus(orderId: String, list: List<String>, orderStatus: OrderStatus, orderItemStatus: OrderItemStatus) = viewModelScope.launch{
        _incomingOrderUiState.update { _incomingOrderUiState.value.copy (loading = true) }
        updateOrderStatusUseCase(orderId,orderStatus){
            if (it is Response.Success){
                _incomingOrderUiState.update {
                    _incomingOrderUiState.value.copy (successUpdate = true, loading = false)
                }
            }
        }
        list.forEach { str ->
            updateOrderItemStatusUseCase(str,orderItemStatus){}
        }
    }
}