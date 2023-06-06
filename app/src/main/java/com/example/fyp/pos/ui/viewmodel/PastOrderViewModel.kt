package com.example.fyp.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.domain.remote_database.GetOrderItemFromRemoteByOrderIdUseCase
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.domain.CompactOrderItem
import com.example.fyp.pos.domain.GetPastOrderUseCase
import com.example.fyp.pos.domain.table.CheckoutTableUseCase
import com.example.fyp.pos.domain.table.GetCheckoutTableOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PastOrderViewModel @Inject constructor(
    private val getPastOrderUseCase: GetPastOrderUseCase,
    private val getOrderItemFromRemoteByOrderIdUseCase: GetOrderItemFromRemoteByOrderIdUseCase,
    private val compactOrderItem: CompactOrderItem,
) : ViewModel() {

    private val _pastOrder = MutableStateFlow<Response<List<Order>>>(Response.Loading)
    val pastOrder = _pastOrder.asStateFlow()

    private val _orderItemListResponse = MutableStateFlow<Response<List<OrderItem>>>(Response.Loading)
    val orderItemListResponse = _orderItemListResponse.asStateFlow()

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder = _currentOrder.asStateFlow()

    lateinit var table: Table

    init {
        getOrderHistory()
    }

    fun getOrderHistory(lastHours: Long = 24) = viewModelScope.launch{
        _pastOrder.update { Response.Loading }
        getPastOrderUseCase(lastHours){ res ->
            res.onEach { it1 ->
                _pastOrder.update { it1 }
            }.launchIn(viewModelScope)
        }
    }

    fun getOrder(id:String) = (_pastOrder.value as Response.Success).data.find { it.orderId == id }

    fun getOrderItemDetails(orderId: String) = viewModelScope.launch{
        _orderItemListResponse.update { Response.Loading }
        getOrderItemFromRemoteByOrderIdUseCase(orderId){ flow ->
            flow.onEach { res ->
                if (res is Response.Success){
                    compactOrderItem(res.data){ after ->
                        _orderItemListResponse.update { Response.Success(after) }
                    }
                } else {
                    _orderItemListResponse.update { res }
                }
            }.launchIn(viewModelScope)
        }
    }

}