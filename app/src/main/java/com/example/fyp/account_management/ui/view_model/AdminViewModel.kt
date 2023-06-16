package com.example.fyp.account_management.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.domain.use_case.GetRegisterStaffTokenUseCase
import com.example.fyp.account_management.domain.use_case.SetRegisterStaffTokenUseCase
import com.example.fyp.account_management.util.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val setRegisterStaffTokenUseCase: SetRegisterStaffTokenUseCase,
    private val getRegisterStaffTokenUseCase: GetRegisterStaffTokenUseCase
    ) : ViewModel(){

    private val _setTokenResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val setTokenResponse = _setTokenResponse.asStateFlow()

    private val _getTokenResponse = MutableStateFlow<Response<String>>(Response.Loading)
    val getTokenResponse = _getTokenResponse.asStateFlow()

    fun setToken(token: String) = viewModelScope.launch {
        _setTokenResponse.value = Response.Loading
        setRegisterStaffTokenUseCase.invoke(token){
            _setTokenResponse.value = it
        }
    }
    fun getToken() = viewModelScope.launch {
        _getTokenResponse.update { getRegisterStaffTokenUseCase() }
    }

}