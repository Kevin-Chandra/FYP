package com.example.fyp.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.domain.remote_database.FinishOrderItemUseCase
import com.example.fyp.ordering_system.domain.remote_database.UpdateOrderItemStatusUseCase
import com.example.fyp.pos.domain.GetOngoingOrderItemUseCase
import com.example.fyp.pos.util.KitchenManageOrderItemEvent
import com.example.fyp.pos.util.KitchenManageOrderItemUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomingOrderItemViewModel @Inject constructor(
    private val getOngoingOrderItemUseCase: GetOngoingOrderItemUseCase,
    private val updateOrderItemStatusUseCase: UpdateOrderItemStatusUseCase,
    private val orderItemFinishUseCase: FinishOrderItemUseCase,
) : ViewModel() {

    private val _manageOrderItemUiState = MutableStateFlow(KitchenManageOrderItemUiState())
    val manageOrderItemUiState = _manageOrderItemUiState.asStateFlow()

    private val _ongoingOrderItems = MutableStateFlow<List<OrderItem>>(emptyList())
    val ongoingOrderItems = _ongoingOrderItems.asStateFlow()

    init {
        getIncomingOrderItem()
    }

    fun onEvent(event: KitchenManageOrderItemEvent){
        when(event){
            is KitchenManageOrderItemEvent.OnFinishOrderItem -> {
                finishOrderItem(event.itemId)
            }
            is KitchenManageOrderItemEvent.OnPrepareOrderItem -> {
                prepareOrderItem(event.itemId)
            }
        }
    }

    private fun prepareOrderItem(itemId: String) = viewModelScope.launch{
        updateOrderItemStatusUseCase(itemId,OrderItemStatus.Preparing){}
    }

    private fun finishOrderItem(itemId: String) = viewModelScope.launch{
        orderItemFinishUseCase(itemId){}
    }

//    private fun deleteOrder(order: Order) = viewModelScope.launch{
//        deleteOrderFromRemoteByOrderUseCase(order){
//            when(it){
//                is Response.Error -> {
//                    _manageOrderUiState.update { it1 -> it1.copy(
//                        errorMessage = it.exception.message,
//                        success = false,
//                        loading = false) }
//                }
//                Response.Loading -> {
//                    _manageOrderUiState.update { _manageOrderUiState.value.copy(loading = true) }
//                }
//                is Response.Success -> {
//                    if (it.data == "Order deleted successfully!"){
//                        _manageOrderUiState.update {
//                            _manageOrderUiState.value.copy(
//                                errorMessage = null,
//                                success = true,
//                                loading = false
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun getIncomingOrderItem() = viewModelScope.launch {
        _manageOrderItemUiState.update { KitchenManageOrderItemUiState(loading = true) }
        getOngoingOrderItemUseCase{
            it.onEach { res ->
                when (res){
                    is Response.Error ->{
                        _manageOrderItemUiState.update { _manageOrderItemUiState.value.copy(
                            errorMessage = res.exception.message,
                            success = false,
                            loading = false
                        ) }
                    }
                    Response.Loading -> {
                        _manageOrderItemUiState.update { _manageOrderItemUiState.value.copy(loading = true) }
                    }
                    is Response.Success -> {
                        _manageOrderItemUiState.update {
                            _manageOrderItemUiState.value.copy(
                                errorMessage = null,
                                success = true,
                                loading = false
                            )
                        }
                        _ongoingOrderItems.update { res.data }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

//    private fun getOngoingOrder() = viewModelScope.launch {
//        getOrderFromRemoteByStatusUseCase(OrderStatus.Ongoing) { it ->
//            it.onEach { res ->
//                when (res){
//                    is Response.Error ->{
//                        _manageOrderUiState.update { _manageOrderUiState.value.copy(
//                            errorMessage = res.exception.message,
//                            success = false,
//                            loading = false
//                        ) }
//                    }
//                    Response.Loading -> {
//                        _manageOrderUiState.update { _manageOrderUiState.value.copy(loading = true) }
//                    }
//                    is Response.Success -> {
//                        _manageOrderUiState.update {
//                            _manageOrderUiState.value.copy(
//                                errorMessage = null,
//                                success = true,
//                                loading = false
//                            )
//                        }
//                        _ongoingOrders.update { res.data }
//                    }
//                }
//            }.launchIn(viewModelScope)
//        }
//    }

//    fun getIncomingOrderItem() = viewModelScope.launch {
//        getOrderFromRemoteByStatusUseCase(OrderStatus.Sent) {
//            _incomingOrder.update { it }
//        }
//    }

//    fun getOrderItemByOrderId(orderId: String, result:(Response<List<OrderItem>>) -> Unit) = viewModelScope.launch {
//        result.invoke(Response.Loading)
//        getOrderItemFromRemoteByOrderIdUseCase(orderId){ it1 ->
//            result.invoke(it1)
//        }
//    }
//
//    private fun updateStatus(orderId: String, list: List<String>, orderStatus: OrderStatus, orderItemStatus: OrderItemStatus) = viewModelScope.launch{
//        _manageOrderUiState.update { _manageOrderUiState.value.copy (loading = true) }
//        updateOrderStatusUseCase(orderId,orderStatus){
//            if (it is Response.Success){
//                _manageOrderUiState.update {
//                    _manageOrderUiState.value.copy (successUpdate = true, loading = false)
//                }
//            }
//        }
//        list.forEach { str ->
//            updateOrderItemStatusUseCase(str,orderItemStatus){}
//        }
//    }
}