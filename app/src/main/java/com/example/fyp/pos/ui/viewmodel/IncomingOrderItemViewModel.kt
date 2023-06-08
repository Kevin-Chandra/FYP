package com.example.fyp.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.domain.remote_database.FinishOrderItemUseCase
import com.example.fyp.ordering_system.domain.remote_database.UpdateOrderItemStatusUseCase
import com.example.fyp.pos.domain.GetOrderItemByStatusUseCase
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
    private val getOrderItemByStatusUseCase: GetOrderItemByStatusUseCase,
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
        _manageOrderItemUiState.update { KitchenManageOrderItemUiState(loading = true) }
        updateOrderItemStatusUseCase(itemId,OrderItemStatus.Preparing){
            _manageOrderItemUiState.value = when(it){
                is Response.Error -> {
                    KitchenManageOrderItemUiState(loading = false,errorMessage = it.exception.message)
                }
                Response.Loading -> {
                    KitchenManageOrderItemUiState(loading = true)
                }
                is Response.Success -> {
                    KitchenManageOrderItemUiState(loading = false, successMessage = it.data, success = true)
                }
            }
        }
    }

    private fun finishOrderItem(itemId: String) = viewModelScope.launch{
        _manageOrderItemUiState.update { KitchenManageOrderItemUiState(loading = true) }
        orderItemFinishUseCase(itemId){
            _manageOrderItemUiState.value = when(it){
                is Response.Error -> {
                    KitchenManageOrderItemUiState(loading = false,errorMessage = it.exception.message)
                }
                Response.Loading -> {
                    KitchenManageOrderItemUiState(loading = true)
                }
                is Response.Success -> {
                    KitchenManageOrderItemUiState(loading = false, successMessage = it.data, success = true)
                }
            }
        }
    }

    private fun getIncomingOrderItem() = viewModelScope.launch {
        _manageOrderItemUiState.update { KitchenManageOrderItemUiState(loading = true) }
        getOrderItemByStatusUseCase(
            statuses = listOf(
                OrderItemStatus.Confirmed,
                OrderItemStatus.Preparing,
                OrderItemStatus.Finished
            ),
            lastDays = 1
        ){
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

    fun resetState() = _manageOrderItemUiState.update { KitchenManageOrderItemUiState() }

}