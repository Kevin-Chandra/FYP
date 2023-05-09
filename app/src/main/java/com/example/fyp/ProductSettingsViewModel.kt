package com.example.fyp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.model.FoodCategory
import com.example.fyp.menucreator.domain.foodCategory.AddFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.DeleteFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.GetFoodCategoryUseCase
import com.example.fyp.menucreator.domain.foodCategory.UpdateFoodCategoryUseCase
import com.example.fyp.menucreator.domain.productSettings.GetServiceChargeUseCase
import com.example.fyp.menucreator.domain.productSettings.GetTaxUseCase
import com.example.fyp.menucreator.domain.productSettings.InsertSettingUseCase
import com.example.fyp.menucreator.domain.productSettings.SetServiceChargeUseCase
import com.example.fyp.menucreator.domain.productSettings.SetTaxUseCase
import com.example.fyp.menucreator.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class ProductSettingsViewModel @Inject constructor(
    private val insertSettingUseCase: InsertSettingUseCase,
    private val setServiceChargeUseCase: SetServiceChargeUseCase,
    private val setTaxUseCase: SetTaxUseCase,
    private val getServiceChargeUseCase: GetServiceChargeUseCase,
    private val getTaxUseCase: GetTaxUseCase,
) : ViewModel(){

    private val _updateState = MutableStateFlow<Response<String>>(Response.Success(""))
    val updateState = _updateState.asStateFlow()

    private val _serviceCharge = MutableSharedFlow<Double>()
    val serviceCharge = _serviceCharge.asSharedFlow()

    private val _tax = MutableSharedFlow<Double>()
    val tax = _tax.asSharedFlow()

    init {
        insertSettingToDatabase()
    }

    fun insertSettingToDatabase() = viewModelScope.launch {
        insertSettingUseCase.invoke {
            if (it is Response.Success){
                getServiceCharge()
                getTax()
            }
        }
    }

    fun setServiceChargePercentage(percentage: String) = viewModelScope.launch{
        try {
            _updateState.update { Response.Loading }
            val value = percentage.toDouble()
            setServiceChargeUseCase.invoke(value){
//                println(it)
                _updateState.value = it
            }
            getServiceCharge()
        } catch (e: Exception){
            _updateState.update { Response.Error(e) }
        }
    }

    fun setTaxPercentage(percentage: String) = viewModelScope.launch{
        try {
            _updateState.update { Response.Loading }
            val value = percentage.toDouble()
            setTaxUseCase.invoke(value){
                _updateState.value = it
            }
            getTax()
        } catch (e: Exception) {
            _updateState.update { Response.Error(e) }
        }
    }

    private fun getServiceCharge() = viewModelScope.launch {
        _serviceCharge.emit(getServiceChargeUseCase())
    }

    private fun getTax() = viewModelScope.launch {
        _tax.emit(getTaxUseCase())
    }

    fun getSettings(){
        getServiceCharge()
        getTax()
    }
}