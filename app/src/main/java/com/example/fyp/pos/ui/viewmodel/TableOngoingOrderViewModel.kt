package com.example.fyp.pos.ui.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.ui.screen.OngoingOrderScreenState
import com.example.fyp.pos.domain.GetOrderItemByStatusUseCase
import com.example.fyp.pos.domain.GetTableOngoingOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TableOngoingOrderViewModel @Inject constructor(
    private val getTableOngoingOrderUseCase: GetTableOngoingOrderUseCase,
    private val getOnOrderItemByStatusUseCase: GetOrderItemByStatusUseCase,
) : ViewModel(){

    private val _tableOngoingOrderList = MutableStateFlow<List<Order>>(emptyList())
    val tableOngoingOrderList = _tableOngoingOrderList.asStateFlow()

    val orderMap = mutableStateMapOf<String,Order>()

    private val orderItemMap = mutableMapOf<String,OrderItem>()

    init {
        getOngoingOrder()
        getOngoingOrderItem()
    }


    private fun getOngoingOrder() = viewModelScope.launch{
        getTableOngoingOrderUseCase(){
            it.onEach { it1 ->
                when (it1){
                    is Response.Error -> TODO()
                    Response.Loading -> TODO()
                    is Response.Success -> {
                        it1.data.associateByTo(orderMap){ order ->
                            order.orderId
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun getOngoingOrderItem() = viewModelScope.launch{
        getOnOrderItemByStatusUseCase(
            listOf(
                OrderItemStatus.Sent,
                OrderItemStatus.Confirmed,
                OrderItemStatus.Preparing,
                OrderItemStatus.Finished
            )
        ){
            it.onEach { it1 ->
                when (it1){
                    is Response.Error -> TODO()
                    Response.Loading -> TODO()
                    is Response.Success -> {
                        it1.data.associateByTo(orderItemMap){ item ->
                            item.orderItemId
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getOrder(orderId: String) = orderMap[orderId]

    fun getOrderItem(id: String) = orderItemMap[id]

//    fun getOngoingOrderItemList(orderId: String) = viewModelScope.launch{
//        _orderingStatusState.update { OngoingOrderScreenState(loading = true) }
//        getOngoingOrderByAccountUseCase(accountId){
//            it.onEach{ res ->
//                when(res){
//                    is Response.Error -> {
//                        _ongoingOrderListStatusState.update { OngoingOrderScreenState(errorMessage = res.exception.message) }
//                    }
//                    Response.Loading -> {
//                        _ongoingOrderListStatusState.update { OngoingOrderScreenState(loading = true) }
//                    }
//                    is Response.Success -> {
//                        _ongoingOrderListStatusState.update { OngoingOrderScreenState(success = true) }
//                        _ongoingOrderList.update { res.data }
//                    }
//                }
//            }.launchIn(viewModelScope)
//        }
//    }
    }