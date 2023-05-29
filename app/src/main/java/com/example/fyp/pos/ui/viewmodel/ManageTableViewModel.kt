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
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.domain.GetOngoingOrderItemUseCase
import com.example.fyp.pos.domain.GetOrderItemByStatusUseCase
import com.example.fyp.pos.domain.UpdateFoodAllTimeSalesUseCase
import com.example.fyp.pos.domain.UpdateOrderHistoryUseCase
import com.example.fyp.pos.domain.table.AddTableUseCase
import com.example.fyp.pos.domain.table.AssignTableUseCase
import com.example.fyp.pos.domain.table.FinishTableUseCase
import com.example.fyp.pos.domain.table.GetTablesUseCase
import com.example.fyp.pos.domain.table.ResetTableUseCase
import com.example.fyp.pos.util.ManageOrderUiState
import com.example.fyp.pos.util.ManageTableEvent
import com.example.fyp.pos.util.ManageTableState
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
class ManageTableViewModel @Inject constructor(
    private val getTablesUseCase: GetTablesUseCase,
    private val addTableUseCase: AddTableUseCase,
    private val finishTableUseCase: FinishTableUseCase,
    private val assignTableUseCase: AssignTableUseCase,
    private val resetTableUseCase: ResetTableUseCase,
    private val updatTab: ResetTableUseCase,
) : ViewModel() {

    private val _manageTableState = MutableStateFlow<Response<String>>(Response.Success(""))
    val manageTableState = _manageTableState.asStateFlow()

    private val _tables = MutableStateFlow<List<Table>>(emptyList())
    val tables = _tables.asStateFlow()

    init {
        getTables()
    }


    fun onEvent(event: ManageTableEvent){
        when(event){
            is ManageTableEvent.OnAddTable -> {
                addTable(event.table)
            }
            is ManageTableEvent.OnFinishTable -> {
                finishTable(event.id)
            }
            is ManageTableEvent.AssignTable -> {
                assignTable(event.id,event.pax,event.label)
            }
            is ManageTableEvent.OnResetTable ->{
                resetTable(event.id)
            }
        }
    }

    private fun addTable(table: Table) = viewModelScope.launch {
        addTableUseCase.invoke(table){
            _manageTableState.update { it }
        }
    }

    private fun resetTable(id: String) = viewModelScope.launch {
        resetTableUseCase(id){
            _manageTableState.update { it }
        }
    }

    private fun assignTable(id: String, pax: Int, label: String?) = viewModelScope.launch {
        assignTableUseCase(id,pax,label){
            _manageTableState.update { it }
        }
    }

    private fun finishTable(id: String) = viewModelScope.launch {
        finishTableUseCase.invoke(id){
            _manageTableState.update { it }
        }
    }

    fun getTable(id: String): Table? {
        return _tables.value.find { it.id == id }
    }

    private fun getTables() = viewModelScope.launch {
//        _manageOrderItemUiState.update { ManageOrderUiState(loading = true) }
        getTablesUseCase{
            it.onEach { res ->
                when (res){
                    is Response.Error ->{
                        //TODO
                    }
                    Response.Loading -> {
//                        _manageOrderItemUiState.update { _manageOrderItemUiState.value.copy(loading = true) }
                    }
                    is Response.Success -> {
                        _tables.value = res.data.sortedBy { it.tableNumber }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

}