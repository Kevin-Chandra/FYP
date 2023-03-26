package com.example.fyp.account_management.ui.view_model

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.domain.use_case.*
import com.example.fyp.account_management.util.*
import com.google.firebase.auth.FirebaseUser
import com.google.protobuf.BoolValueOrBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import java.security.interfaces.RSAMultiPrimePrivateCrtKey
import java.util.*
import javax.inject.Inject


/**
 * This view model served to register and edit account
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val setRegisterStaffTokenUseCase: SetRegisterStaffTokenUseCase,
    private val getRegisterStaffTokenUseCase: GetRegisterStaffTokenUseCase
    ) : ViewModel(){

    // TODO RECHECK ALL register staff

    private val _loadingState = MutableStateFlow<Response<Boolean>>(Response.Loading)
    val loadingState = _loadingState.asStateFlow()

    private val _registerState = MutableStateFlow(StaffRegistrationState())
    val registerState = _registerState.asStateFlow()

    private val _setTokenResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val setTokenResponse = _setTokenResponse.asStateFlow()

    private val _getTokenResponse = MutableStateFlow<Response<String>>(Response.Loading)
    val getTokenResponse = _getTokenResponse.asStateFlow()

    private val _updateResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val updateResponse = _updateResponse.asStateFlow()

//    fun onEvent(event: StaffRegistrationEvent) {
//        when(event) {
//            is StaffRegistrationEvent.EmailChanged -> {
//                _registerState.value = registerState.value.copy(email = event.email)
//            }
//            is StaffRegistrationEvent.FirstNameChanged -> {
//                _registerState.value = registerState.value.copy(fname = event.fname)
//            }
//            is StaffRegistrationEvent.LastNameChanged -> {
//                _registerState.value = registerState.value.copy(lname = event.lname)
//            }
//            is StaffRegistrationEvent.PhoneChanged -> {
//                _registerState.value = registerState.value.copy(phone = event.phone)
//            }
//            is StaffRegistrationEvent.BirthdayChanged -> {
//                _registerState.value = registerState.value.copy(birthday = event.date)
//            }
//            is StaffRegistrationEvent.AddressChanged -> {
//                _registerState.value = registerState.value.copy(address = event.address)
//            }
//            is StaffRegistrationEvent.Submit -> {
//                submitData()
//            }
//        }
//    }

    fun setToken(token: String) = viewModelScope.launch {
        _setTokenResponse.value = Response.Loading
        setRegisterStaffTokenUseCase.invoke(token){
            _setTokenResponse.value = it
        }
    }

    fun getToken() = getRegisterStaffTokenUseCase.invoke {
        viewModelScope.launch{
            it.collect{ response ->
                _getTokenResponse.value = response
            }
        }
    }

    private fun getStaffAccount(
        first_name: String,
        last_name: String?,
        phone: String,
        email: String,
        address: String?,
        birthday: Date?
    ) : Account {
        return Account(
            "",
            first_name,
            last_name?: "",
            phone,
            email,
            address?:"",
            birthday,
            null,
            null,
            AccountType.Staff,
            Date()
        )
    }

//    fun getSession() = viewModelScope.launch{
//        reset()
//        _user = getSessionUseCase() as CustomerAccount
//
//        if (_user != null) {
//            println("user uri " +user.profileUri)
//            loadToRegisterState()
//            command = Constants.Command.EDIT
//            _loadingState.value = Response.Success(true)
//        } else {
//            _loadingState.value = Response.Error(Exception("User Not Found"))
//        }
//    }

//    private fun loadToRegisterState() {
//        onEvent(RegistrationEvent.AddressChanged(user.address))
//        onEvent(RegistrationEvent.PhoneChanged(user.phone))
//        onEvent(RegistrationEvent.FirstNameChanged(user.first_name))
//        onEvent(RegistrationEvent.LastNameChanged(user.last_name))
////        user.profileUri?.let { RegistrationEvent.ImageChanged(it.toUri()) }?.let { onEvent(it) }
//        user.birthday?.let { RegistrationEvent.BirthdayChanged(it) }?.let { onEvent(it) }
//    }

//    private fun reset(){
//        _registerState.value = RegistrationState()
//        _updateResponse.value = Response.Success("")
//        _registerResponse.value = Response.Success("")
//        _loadingState.value = Response.Loading
//    }

}