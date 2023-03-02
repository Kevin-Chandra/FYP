package com.example.fyp.account_management.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.CustomerAccount
import com.example.fyp.account_management.domain.use_case.ChangePasswordUseCase
import com.example.fyp.account_management.domain.use_case.GetSessionUseCase
import com.example.fyp.account_management.domain.use_case.ValidatePasswordUseCase
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class UpdatePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val getSessionUseCase: GetSessionUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase
): ViewModel() {

    private val _changePassState = MutableStateFlow<Response<String>>(Response.Success(""))
    val changePassState = _changePassState.asStateFlow()

    private var _user: CustomerAccount? = null
    val user get() = _user!!

    fun getSession() = viewModelScope.launch{
        _user = getSessionUseCase() as CustomerAccount

        if (_user == null) {
            _changePassState.value = Response.Error(Exception("Account unavailable or not logged in"))
        }
    }

    fun updatePassword(oldPass: String,newPass: String){
        _changePassState.value = Response.Loading
        if (_user == null) {
            _changePassState.value = Response.Error(Exception("Account unavailable or not logged in"))
        } else {
            if (validate(newPass))
                changePasswordUseCase.invoke(oldPass,newPass){
                    _changePassState.value = it
                }
        }

    }

    private fun validate(pass : String) : Boolean{
        val result = validatePasswordUseCase(pass)
        return if (!result.successful) {
            _changePassState.value = Response.Error(Exception(result.errorMessage))
            false
        } else {
            true
        }
    }
}