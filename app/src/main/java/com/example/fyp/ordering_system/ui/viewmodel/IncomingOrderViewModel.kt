package com.example.fyp.ordering_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.domain.remote_database.DeleteOrderFromRemoteByOrderUseCase
import com.example.fyp.ordering_system.domain.remote_database.GetOrderFromRemoteByStatusUseCase
import com.example.fyp.ordering_system.domain.remote_database.GetOrderItemFromRemoteByOrderIdUseCase
import com.example.fyp.ordering_system.domain.remote_database.UpdateOrderItemStatusUseCase
import com.example.fyp.ordering_system.domain.remote_database.UpdateOrderStatusUseCase
import com.example.fyp.ordering_system.ui.state.IncomingOrderUiState
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
    private val deleteOrderFromRemoteByOrderUseCase: DeleteOrderFromRemoteByOrderUseCase,
) : ViewModel() {

    private val _manageOrderUiState = MutableStateFlow(IncomingOrderUiState())
    val manageOrderUiState = _manageOrderUiState.asStateFlow()

    private val _ongoingOrders = MutableStateFlow<List<Order>>(emptyList())
    val ongoingOrders = _ongoingOrders.asStateFlow()

    private val _incomingOrders = MutableStateFlow<List<Order>>(emptyList())
    val incomingOrders = _incomingOrders.asStateFlow()

    init {
        getIncomingOrder()
        getOngoingOrder()
    }

    private val _modifierItems = MutableStateFlow<UiState<List<ModifierItem>>>(UiState.Loading)
    val modifierItems = _modifierItems.asStateFlow()

    fun onEvent(event: ManageOrderEvent){
        when(event){
            is ManageOrderEvent.OnAcceptOrder -> {
                updateStatus(event.orderId,event.list,OrderStatus.Ongoing,OrderItemStatus.Confirmed)
            }
            is ManageOrderEvent.OnRejectOrder -> {
                updateStatus(event.orderId,event.list,OrderStatus.Rejected,OrderItemStatus.Cancelled)
            }
            is ManageOrderEvent.OnDeleteOrder -> {
                deleteOrder(event.order)
            }
        }
    }

    private fun deleteOrder(order: Order) = viewModelScope.launch{
        deleteOrderFromRemoteByOrderUseCase(order){
            when(it){
                is Response.Error -> {
                    _manageOrderUiState.update { it1 -> it1.copy(
                        errorMessage = it.exception.message,
                        success = false,
                        loading = false) }
                }
                Response.Loading -> {
                    _manageOrderUiState.update { _manageOrderUiState.value.copy(loading = true) }
                }
                is Response.Success -> {
                    if (it.data == "Order deleted successfully!"){
                        _manageOrderUiState.update {
                            _manageOrderUiState.value.copy(
                                errorMessage = null,
                                success = true,
                                loading = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getIncomingOrder() = viewModelScope.launch {
        _manageOrderUiState.update { IncomingOrderUiState(loading = true) }
        getOrderFromRemoteByStatusUseCase(OrderStatus.Sent) { it ->
            it.onEach { res ->
                when (res){
                    is Response.Error ->{
                        _manageOrderUiState.update { _manageOrderUiState.value.copy(
                            errorMessage = res.exception.message,
                            success = false,
                            loading = false
                        ) }
                    }
                    Response.Loading -> {
                        _manageOrderUiState.update { _manageOrderUiState.value.copy(loading = true) }
                    }
                    is Response.Success -> {
                        _manageOrderUiState.update {
                            _manageOrderUiState.value.copy(
                                errorMessage = null,
                                success = true,
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
                        _manageOrderUiState.update { _manageOrderUiState.value.copy(
                            errorMessage = res.exception.message,
                            success = false,
                            loading = false
                        ) }
                    }
                    Response.Loading -> {
                        _manageOrderUiState.update { _manageOrderUiState.value.copy(loading = true) }
                    }
                    is Response.Success -> {
                        _manageOrderUiState.update {
                            _manageOrderUiState.value.copy(
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
        _manageOrderUiState.update { _manageOrderUiState.value.copy (loading = true) }
        updateOrderStatusUseCase(orderId,orderStatus){
            if (it is Response.Success){
                _manageOrderUiState.update {
                    _manageOrderUiState.value.copy (successUpdate = true, loading = false)
                }
            }
        }
        list.forEach { str ->
            updateOrderItemStatusUseCase(str,orderItemStatus){}
        }
    }
}