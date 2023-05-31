package com.example.fyp.pos.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.model.TableStatus
import com.example.fyp.pos.domain.table.AddTableUseCase
import com.example.fyp.pos.domain.table.AssignTableUseCase
import com.example.fyp.pos.domain.table.FinishTableUseCase
import com.example.fyp.pos.domain.table.GetTablesUseCase
import com.example.fyp.pos.domain.table.RemoveTableUseCase
import com.example.fyp.pos.domain.table.ResetTableUseCase
import com.example.fyp.pos.domain.table.SetTableAvailabilityUseCase
import com.example.fyp.pos.domain.table.UpdateTableUseCase
import com.example.fyp.pos.util.AddEditTableEvent
import com.example.fyp.pos.util.AddEditTableState
import com.example.fyp.pos.util.ManageTableEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.IllegalFormatConversionException
import javax.inject.Inject

const val TAG = "ManageTableViewModel"
@HiltViewModel
class ManageTableViewModel @Inject constructor(
    private val getTablesUseCase: GetTablesUseCase,
    private val addTableUseCase: AddTableUseCase,
    private val updateTableUseCase: UpdateTableUseCase,
    private val finishTableUseCase: FinishTableUseCase,
    private val assignTableUseCase: AssignTableUseCase,
    private val setTableAvailabilityUseCase: SetTableAvailabilityUseCase,
    private val resetTableUseCase: ResetTableUseCase,
    private val removeTableUseCase: RemoveTableUseCase,
) : ViewModel() {

    private val _manageTableState = MutableStateFlow<Response<String>>(Response.Success(""))
    val manageTableState = _manageTableState.asStateFlow()

    private val _addEditTableState = MutableStateFlow(AddEditTableState())
    val addEditTableState = _addEditTableState.asStateFlow()

    private val _tables = MutableStateFlow<List<Table>>(emptyList())
    val tables = _tables.asStateFlow()

    var currentEditTable : Table? = null

    init {
        getTables()
    }

    fun onEvent(event: ManageTableEvent){
        when(event){
            is ManageTableEvent.OnFinishTable -> {
                finishTable(event.id)
            }
            is ManageTableEvent.AssignTable -> {
                assignTable(event.id,event.pax,event.label)
            }
            is ManageTableEvent.OnResetTable ->{
                resetTable(event.id)
            }
            is ManageTableEvent.OnSetTableAvailability -> {
                setTableAvailability(event.id,event.availability)
            }
            is ManageTableEvent.OnDeleteTable -> {
                removeTable(event.id)
            }
        }
    }

    fun onAddEditTableEvent(event: AddEditTableEvent){
        when(event){
            is AddEditTableEvent.OnPaxCapacityChanged -> {
                _addEditTableState.update { it.copy(tablePaxCapacity = event.pax) }
            }
            AddEditTableEvent.OnSave -> {
                saveTable()
            }
            is AddEditTableEvent.OnTableNameChanged -> {
                _addEditTableState.update { it.copy(tableName = event.name) }
            }
            is AddEditTableEvent.OnTableNumberChanged -> {
                _addEditTableState.update { it.copy(tableNumber = event.tableNumber) }
                validateTableNumber(event.tableNumber,currentEditTable?.tableNumber ?: -1)
            }
            AddEditTableEvent.OnReset -> {
                resetAddEditTableState()
            }
        }
    }

    private fun saveTable() = viewModelScope.launch{
        if (addEditTableState.value.edit)
            validateTableNumber(addEditTableState.value.tableNumber,currentEditTable!!.tableNumber)
        else
            validateTableNumber(addEditTableState.value.tableNumber)

        _addEditTableState.update { it.copy(loading = true) }

        if (addEditTableState.value.tableNameError == null && addEditTableState.value.tableNumberError == null){

            if (addEditTableState.value.edit){
                updateTableUseCase.invoke(getTable(currentEditTable!!)){
                    when (it){
                        is Response.Error -> {
                            Log.d(TAG, "update Table: ${it.exception.message}")
                            _addEditTableState.update { state -> state.copy(error = it.exception.message, loading = false) }
                        }
                        Response.Loading -> {
                            _addEditTableState.update { state -> state.copy(loading = true) }
                        }
                        is Response.Success -> {
                            _addEditTableState.update { state -> state.copy(success = true, successMessage = it.data, loading = false) }
                        }
                    }
                }
            } else {
                addTableUseCase.invoke(getTable()){
                    when (it){
                        is Response.Error -> {
                            Log.d(TAG, "saveTable: ${it.exception.message}")
                            _addEditTableState.update { state -> state.copy(error = it.exception.message, loading = false) }
                        }
                        Response.Loading -> {
                            _addEditTableState.update { state -> state.copy(loading = true) }
                        }
                        is Response.Success -> {
                            _addEditTableState.update { state -> state.copy(success = true, successMessage = it.data, loading = false) }
                        }
                    }
                }
            }

        } else {
            _addEditTableState.update { state -> state.copy(error = "Error!", loading = false) }
        }
    }

    fun editTable(table: Table) = viewModelScope.launch{
        resetAddEditTableState()
        _addEditTableState.update {
            AddEditTableState(
                tableNumber = table.tableNumber.toString(),
                tableName = table.name,
                tablePaxCapacity = table.paxCapacity,
                edit = true
            )
        }
        currentEditTable = table
    }

    private fun getTable(currentTable : Table = Table()) = currentTable.copy(
        tableNumber = addEditTableState.value.tableNumber.toInt(),
        name = addEditTableState.value.tableName,
        paxCapacity = addEditTableState.value.tablePaxCapacity
    )

    private fun validateTableNumber(number: String,exceptNumber: Int = -1) {
        try {
            val tableNumber = number.toInt()
            if (tableNumber < 0)
                throw Exception("Table number should not be negative!")
            if (tables.value.any { it.tableNumber == tableNumber }){
                if (exceptNumber == tableNumber ){
                    _addEditTableState.update { it.copy(tableNumberError = null, loading = false) }
                } else {
                    _addEditTableState.update { it.copy(tableNumberError = "Table number already exist!", loading = false) }
                }
            } else{
                _addEditTableState.update { it.copy(tableNumberError = null, loading = false) }
            }
        } catch (e:Exception){
            if (e is IllegalFormatConversionException){
                _addEditTableState.update { it.copy(tableNumberError = "Invalid integer given!", loading = false) }
            } else {
                _addEditTableState.update { it.copy(tableNumberError = e.message, loading = false) }
            }
        }
    }

    private fun resetAddEditTableState(){
        _addEditTableState.update { AddEditTableState() }
        currentEditTable = null
    }

    private fun resetTable(id: String) = viewModelScope.launch {
        resetTableUseCase(id){
            _manageTableState.update { it }
        }
    }

    private fun setTableAvailability(id: String, available: Boolean) = viewModelScope.launch {
        setTableAvailabilityUseCase(id, if (available) TableStatus.Available else TableStatus.Unavailable){
            _manageTableState.update { it }
        }
    }

    private fun assignTable(id: String, pax: Int, label: String?) = viewModelScope.launch {
        assignTableUseCase(id,pax,label){
            _manageTableState.update { it }
        }
    }

    private fun removeTable(id: String) = viewModelScope.launch {
        removeTableUseCase(id){
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
                        _tables.value = res.data
//                            .sortedBy { it.tableNumber }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

}