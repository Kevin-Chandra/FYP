package com.example.fyp.ordering_system.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.domain.local_database.DeleteAllOrderItemUseCase
import com.example.fyp.ordering_system.domain.remote_database.GetOngoingOrderByAccountUseCase
import com.example.fyp.ordering_system.domain.remote_database.GetOrderStatusFromRemoteUseCase
import com.example.fyp.ordering_system.ui.screen.OngoingOrderScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OngoingOrderViewModel @Inject constructor(
    private val getOrderStatusFromRemoteUseCase: GetOrderStatusFromRemoteUseCase,
    private val deleteAllOrderItemUseCase: DeleteAllOrderItemUseCase,
    private val getOngoingOrderByAccountUseCase: GetOngoingOrderByAccountUseCase,
) : ViewModel(){

    private val _orderingStatusState = MutableStateFlow(OngoingOrderScreenState(loading = true))
    val orderingStatusState = _orderingStatusState.asStateFlow()

    private val _ongoingOrderListStatusState = MutableStateFlow(OngoingOrderScreenState(loading = true))
    val ongoingOrderListStatusState = _ongoingOrderListStatusState.asStateFlow()

    private val _ongoingOrderList = MutableStateFlow<List<Order>>(emptyList())
    val ongoingOrderList = _ongoingOrderList.asStateFlow()

    fun getOrderStatus(id: String) = viewModelScope.launch{
        _orderingStatusState.update { OngoingOrderScreenState(loading = true) }
        getOrderStatusFromRemoteUseCase(id){
            it.onEach{ res ->
                when(res){
                    is Response.Error -> {
                        _orderingStatusState.update { OngoingOrderScreenState(errorMessage = res.exception.message) }
                    }
                    Response.Loading -> {
                        _orderingStatusState.update { OngoingOrderScreenState(loading = true) }
                    }
                    is Response.Success -> {
                        _orderingStatusState.update {
                            when (res.data.orderStatus) {
                                OrderStatus.Rejected -> {
                                    OngoingOrderScreenState(success = true, successMessage = "Status updated!", status = "Rejected")
                                }
                                OrderStatus.Sent -> {
                                    OngoingOrderScreenState(success = true, successMessage = "Status updated!",status = "Processing")
                                }
                                OrderStatus.Confirmed -> {
                                    OngoingOrderScreenState(success = true, successMessage = "Status updated!",status = "Confirmed")
                                }
                                OrderStatus.Ongoing -> {
                                    deleteAllOrderItem()
                                    OngoingOrderScreenState(success = true, successMessage = "Status updated!",status = "Preparing")
                                }
                                OrderStatus.Finished -> {
                                    OngoingOrderScreenState(success = true, successMessage = "Status updated!",status = "Finished")
                                }
                            }
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getOngoingOrderList(accountId: String) = viewModelScope.launch{
        _orderingStatusState.update { OngoingOrderScreenState(loading = true) }
        getOngoingOrderByAccountUseCase(accountId){
            it.onEach{ res ->
                when(res){
                    is Response.Error -> {
                        _ongoingOrderListStatusState.update { OngoingOrderScreenState(errorMessage = res.exception.message) }
                    }
                    Response.Loading -> {
                        _ongoingOrderListStatusState.update { OngoingOrderScreenState(loading = true) }
                    }
                    is Response.Success -> {
                        _ongoingOrderListStatusState.update { OngoingOrderScreenState(success = true) }
                        _ongoingOrderList.update { res.data }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun deleteAllOrderItem() = viewModelScope.launch {
        deleteAllOrderItemUseCase()
    }

}