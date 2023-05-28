package com.example.fyp.pos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.domain.table.CheckoutTableUseCase
import com.example.fyp.pos.domain.table.GetCheckoutTableOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCheckoutTableOrderUseCase: GetCheckoutTableOrderUseCase,
    private val checkoutTableOrderUseCase: CheckoutTableUseCase,
) : ViewModel() {

    private val _checkoutState = MutableStateFlow<Response<String>>(Response.Success(""))
    val checkoutState = _checkoutState.asStateFlow()

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder = _currentOrder.asStateFlow()

    lateinit var table: Table

    fun initTable(table: Table?){
        if(table == null){
            _checkoutState.update { Response.Error(Exception("Table is null!"))}
            return
        }
        this.table = table
        getCheckoutOrder(table)
    }

    private fun getCheckoutOrder(table: Table) = viewModelScope.launch{
        _checkoutState.update { Response.Loading }
        getCheckoutTableOrderUseCase(table){ order ->
            when(order){
                is Response.Error -> {
                    _checkoutState.update { Response.Error(order.exception) }
                }
                Response.Loading -> {
                    _checkoutState.update { Response.Loading }
                }
                is Response.Success -> {
                    _currentOrder.update { order.data }
                    _checkoutState.update { Response.Success("Order retrieved!") }
                }
            }
        }
    }

    fun checkoutTable(table: Table,order: Order) = viewModelScope.launch{
        _checkoutState.update { Response.Loading }
        checkoutTableOrderUseCase(table,order){ res ->
            _checkoutState.update { res }
        }
    }
}