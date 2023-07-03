package com.example.fyp.account_management.ui.view_model

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.domain.use_case.*
import com.example.fyp.account_management.util.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainAuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getSessionUseCase: GetSessionUseCase,
    private val getSessionFlowUseCase: GetSessionFlowUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<Response<Boolean>>(Response.Success(false))
    val loginState = _loginState.asStateFlow()

    private val _resetState = MutableStateFlow<Response<String>>(Response.Success(""))
    val resetState = _resetState.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<Response<String>>(Response.Success(""))
    val deleteAccountState = _deleteAccountState.asStateFlow()

    private var _account : Account? = null
    val accountState = mutableStateOf<Account?>(null)

    var loading = mutableStateOf(true)

    init {
        getAccountFlow()
        getAccountSession()
    }

    fun login(email: String, password: String) = viewModelScope.launch{
        _loginState.emit(Response.Loading)
            loginUseCase.invoke(email,password){
                _loginState.value = it
            }
    }

    fun resetPassword(email: String) = viewModelScope.launch(Dispatchers.Main){
        _resetState.value = Response.Loading
        resetPasswordUseCase.invoke(email){
            _resetState.value = it
        }
    }

    private fun getAccountSession() = viewModelScope.launch(Dispatchers.IO){
        _account = getSessionUseCase()
    }

    private fun getAccountFlow() = viewModelScope.launch{
        getSessionFlowUseCase().onEach { account: Account? ->
            _account = account
            accountState.value = account
            withContext(Dispatchers.Main) {
                loading.value = false
            }
        }.launchIn(viewModelScope)
    }

    fun getSession(online: Boolean = false,result: (Account?) -> Unit) = viewModelScope.launch(Dispatchers.Main){
        loading.value = true
        if (_account == null || online){
            result.invoke(getSessionUseCase())
        } else {
            loading.value = false
            result.invoke(_account)
        }
    }
    fun logout( result : () -> Unit){
        logoutUseCase.invoke(result)
    }

    fun deleteAccount(password: String) = viewModelScope.launch{
        _deleteAccountState.value = Response.Loading
        if (password.isNotEmpty()){
            deleteAccountUseCase.invoke(password){
                _deleteAccountState.value = it
            }
        } else {
            _deleteAccountState.update { Response.Error(Exception("Password is not provided!")) }
        }
    }

    fun resetDeleteAccountState() = _deleteAccountState.update { Response.Success("") }

}