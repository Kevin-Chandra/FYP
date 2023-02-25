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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainAuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val getSessionUseCase: GetSessionUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    var user: Account? = null

    private val _loginState = MutableStateFlow<Response<Boolean>>(Response.Success(false))
    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) = viewModelScope.launch{
        _loginState.emit(Response.Loading)
            loginUseCase.invoke(email,password){
                _loginState.value = it
            }

    }

    fun getSession( result : (Account?) -> Unit ) = viewModelScope.launch(Dispatchers.Main){
        result.invoke(getSessionUseCase.invoke())
    }

    fun logout( result : () -> Unit){
        logoutUseCase.invoke(result)
    }

}