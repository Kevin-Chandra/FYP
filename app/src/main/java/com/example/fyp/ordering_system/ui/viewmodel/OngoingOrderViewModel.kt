package com.example.fyp.ordering_system.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.Result
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.domain.productSettings.GetServiceChargeUseCase
import com.example.fyp.menucreator.domain.productSettings.GetTaxUseCase
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.domain.DeleteAllOrderItemUseCase
import com.example.fyp.ordering_system.domain.DeleteItemFromCartUseCase
import com.example.fyp.ordering_system.domain.GetCartUseCase
import com.example.fyp.ordering_system.domain.GetOngoingOrderByAccountUseCase
import com.example.fyp.ordering_system.domain.SubmitOrderUseCase
import com.example.fyp.ordering_system.domain.UpsertToCartUseCase
import com.example.fyp.ordering_system.domain.validation.GetOrderStatusFromRemoteUseCase
import com.example.fyp.ordering_system.ui.screen.OngoingOrderScreenState
import com.example.fyp.ordering_system.util.AddToCartEvent
import com.example.fyp.ordering_system.util.AddToCartState
import com.example.fyp.ordering_system.util.OrderingEvent
import com.example.fyp.ordering_system.util.OrderingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OngoingOrderViewModel @Inject constructor(
    private val getOrderStatusFromRemoteUseCase: GetOrderStatusFromRemoteUseCase,
    private val deleteAllOrderItemUseCase: DeleteAllOrderItemUseCase,
    private val getOngoingOrderByAccountUseCase: GetOngoingOrderByAccountUseCase,
) : ViewModel(){

    private val _orderingStatusState = MutableStateFlow(OngoingOrderScreenState(loading = true))
    val orderingStatusState = _orderingStatusState.asStateFlow()

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
                                    OngoingOrderScreenState(success = true, status = "Rejected")
                                }
                                OrderStatus.Sent -> {
                                    OngoingOrderScreenState(success = true, status = "Processing")
                                }
                                OrderStatus.Confirmed -> {
                                    OngoingOrderScreenState(success = true, status = "Confirmed")
                                }
                                OrderStatus.Ongoing -> {
                                    deleteAllOrderItem()
                                    OngoingOrderScreenState(success = true, status = "Preparing")
                                }
                                OrderStatus.Finished -> {
                                    OngoingOrderScreenState(success = true, status = "Finished")
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
                        _orderingStatusState.update { OngoingOrderScreenState(errorMessage = res.exception.message) }
                    }
                    Response.Loading -> {
                        _orderingStatusState.update { OngoingOrderScreenState(loading = true) }
                    }
                    is Response.Success -> {
                        _orderingStatusState.update { OngoingOrderScreenState(success = true) }
                        _ongoingOrderList.update { res.data }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun reset(){
        _ongoingOrderList.value = emptyList()
        _orderingStatusState.value = OngoingOrderScreenState()
    }

    private fun deleteAllOrderItem() = viewModelScope.launch {
        deleteAllOrderItemUseCase()
    }

}