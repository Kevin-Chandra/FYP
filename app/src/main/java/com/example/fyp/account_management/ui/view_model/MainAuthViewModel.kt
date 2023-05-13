package com.example.fyp.account_management.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.domain.use_case.*
import com.example.fyp.account_management.ui.AuthState
import com.example.fyp.account_management.util.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainAuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getSessionUseCase: GetSessionUseCase,
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

    private var account : Account? = null

    init {
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
        account = getSessionUseCase.invoke()
    }

    fun getSession(online: Boolean = false,result: (Account?) -> Unit) = viewModelScope.launch(Dispatchers.Main){
        if (account == null || online){
            println("Request session online")
            result.invoke(getSessionUseCase())
        } else {
            println("Request session locally")
            result.invoke(account)
        }
    }

    fun logout( result : () -> Unit){
        logoutUseCase.invoke(result)
    }

    fun deleteAccount(password: String) = viewModelScope.launch{
        _deleteAccountState.value = Response.Loading
        deleteAccountUseCase.invoke(password){
            _deleteAccountState.value = it
        }
    }

}