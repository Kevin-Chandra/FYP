package com.example.fyp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.domain.productSettings.GetServiceChargeUseCase
import com.example.fyp.menucreator.domain.productSettings.GetTaxUseCase
import com.example.fyp.menucreator.domain.productSettings.GetVersionNameUseCase
import com.example.fyp.menucreator.domain.productSettings.InsertSettingUseCase
import com.example.fyp.menucreator.domain.productSettings.SetServiceChargeUseCase
import com.example.fyp.menucreator.domain.productSettings.SetTaxUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getVersionNameUseCase: GetVersionNameUseCase,
) : ViewModel(){

    private val _updateState = MutableStateFlow<Response<String>>(Response.Success(""))
    val updateState = _updateState.asStateFlow()

    private val _serviceCharge = MutableSharedFlow<Double>()
    val serviceCharge = _serviceCharge.asSharedFlow()

    private val _tax = MutableSharedFlow<Double>()
    val tax = _tax.asSharedFlow()

    private val _versionName = MutableStateFlow<Response<Pair<String,String>>>(Response.Loading)
    val versionName = _versionName.asStateFlow()

    init {
        insertSettingToDatabase()
    }

    fun getVersionName() = viewModelScope.launch{
        _versionName.value =  getVersionNameUseCase()
    }

    fun insertSettingToDatabase() = viewModelScope.launch {
        insertSettingUseCase.invoke {
            if (it is Response.Success){
                getServiceCharge()
                getTax()
            }
        }
    }

    fun setServiceChargePercentage(account: Account, percentage: String) = viewModelScope.launch{
        try {
            _updateState.update { Response.Loading }
            val value = percentage.toDouble()
            setServiceChargeUseCase.invoke(account,value){
                _updateState.value = it
            }
            getServiceCharge()
        } catch (e: Exception){
            _updateState.update { Response.Error(e) }
        }
    }

    fun setTaxPercentage(account: Account,percentage: String) = viewModelScope.launch{
        try {
            _updateState.update { Response.Loading }
            val value = percentage.toDouble()
            setTaxUseCase.invoke(account,value){
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