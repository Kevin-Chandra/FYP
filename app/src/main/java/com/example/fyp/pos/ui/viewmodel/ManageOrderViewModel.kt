package com.example.fyp.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.domain.remote_database.FinishOrderUseCase
import com.example.fyp.ordering_system.domain.remote_database.GetOngoingOrderFromRemoteUseCase
import com.example.fyp.ordering_system.domain.remote_database.UpdateOrderStatusUseCase
import com.example.fyp.pos.domain.GetOngoingOrderItemUseCase
import com.example.fyp.pos.domain.GetOrderItemByStatusUseCase
import com.example.fyp.pos.domain.UpdateFoodAllTimeSalesUseCase
import com.example.fyp.pos.domain.UpdateOrderHistoryUseCase
import com.example.fyp.pos.util.ManageOrderUiState
import com.example.fyp.pos.util.PosManageOrderEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageOrderViewModel @Inject constructor(
    private val getOngoingOrderFromRemoteUseCase: GetOngoingOrderFromRemoteUseCase,
    private val getOrderItemByStatusUseCase: GetOrderItemByStatusUseCase,
    private val updateFoodAllTimeSalesUseCase: UpdateFoodAllTimeSalesUseCase,
    private val updateOrderHistoryUseCase: UpdateOrderHistoryUseCase,
    private val finishOrderUseCase: FinishOrderUseCase,
) : ViewModel() {

    private val _manageOrderItemUiState = MutableStateFlow(ManageOrderUiState())
    val manageOrderItemUiState = _manageOrderItemUiState.asStateFlow()

    private val _ongoingOrder = MutableStateFlow<List<Order>>(emptyList())
    val ongoingOrder = _ongoingOrder.asStateFlow()

    private val _ongoingOrderItem = MutableStateFlow<Map<String,OrderItem>>(emptyMap())
    val ongoingOrderItem = _ongoingOrderItem.asStateFlow()

    init {
        getOngoingOrderItem()
        getOngoingOrder()
    }

    private fun getOngoingOrderItem() = viewModelScope.launch {
        getOrderItemByStatusUseCase(listOf(
            OrderItemStatus.Sent,
            OrderItemStatus.Confirmed,
            OrderItemStatus.Preparing,
            OrderItemStatus.Finished,
        )){
            it.onEach { res ->
                if (res is Response.Success){
                    _ongoingOrderItem.update { res.data.associateBy { it1 -> it1.orderItemId } }
                    println(_ongoingOrderItem.value)
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onEvent(event: PosManageOrderEvent){
        when(event){
            is PosManageOrderEvent.OnFinishOrderItem -> {
                finishOrder(event.order)
            }
        }
    }

    private fun finishOrder(order: Order) = viewModelScope.launch {
        finishOrderUseCase(order.orderId){
            if (it is Response.Success){
                order.orderList.forEach {
                    launch {
                        val orderItem = _ongoingOrderItem.value[it] ?: return@launch
                        updateFoodAllTimeSalesUseCase.invoke(orderItem.foodId,orderItem.quantity){}
                    }
                }
                launch {
                    updateOrderHistoryUseCase.invoke(order.orderBy,order.orderId){}
                }
            }
        }
    }
//
//    private fun prepareOrderItem(itemId: String) = viewModelScope.launch{
//        updateOrderItemStatusUseCase(itemId,OrderItemStatus.Preparing){}
//    }
//
//    private fun finishOrderItem(itemId: String) = viewModelScope.launch{
//        orderItemFinishUseCase(itemId){}
//    }

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

    private fun getOngoingOrder() = viewModelScope.launch {
        _manageOrderItemUiState.update { ManageOrderUiState(loading = true) }
        getOngoingOrderFromRemoteUseCase{
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
                        _ongoingOrder.update { res.data }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

}