package com.example.fyp.account_management.ui.view_model

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.data.model.CustomerAccount
import com.example.fyp.account_management.data.model.StaffAccount
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
class RegisterStaffViewModel @Inject constructor(
    private val registerStaffUseCase: RegisterStaffUseCase,
    private val editAccountUseCase: EditAccountUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateNameUseCase: ValidateNameUseCase,
    private val validatePhoneUseCase: ValidatePhoneUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val getSessionUseCase: GetSessionUseCase,
    ) : ViewModel(){

    // TODO RECHECK ALL register staff
    private var command: String = Constants.Command.ADD

    private var _user: CustomerAccount? = null
    val user get() = _user!!

    private val _loadingState = MutableStateFlow<Response<Boolean>>(Response.Loading)
    val loadingState = _loadingState.asStateFlow()

    private val _registerState = MutableStateFlow(StaffRegistrationState())
    val registerState = _registerState.asStateFlow()

    private val _registerResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val registerResponse = _registerResponse.asStateFlow()

    private val _updateResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val updateResponse = _updateResponse.asStateFlow()

    fun onEvent(event: StaffRegistrationEvent) {
        when(event) {
            is StaffRegistrationEvent.EmailChanged -> {
                _registerState.value = registerState.value.copy(email = event.email)
            }
            is StaffRegistrationEvent.FirstNameChanged -> {
                _registerState.value = registerState.value.copy(fname = event.fname)
            }
            is StaffRegistrationEvent.LastNameChanged -> {
                _registerState.value = registerState.value.copy(lname = event.lname)
            }
            is StaffRegistrationEvent.PhoneChanged -> {
                _registerState.value = registerState.value.copy(phone = event.phone)
            }
            is StaffRegistrationEvent.BirthdayChanged -> {
                _registerState.value = registerState.value.copy(birthday = event.date)
            }
            is StaffRegistrationEvent.AddressChanged -> {
                _registerState.value = registerState.value.copy(address = event.address)
            }
            is StaffRegistrationEvent.Submit -> {
                submitData()
            }
        }
    }

    private fun submitData() {
        var emailResult = ValidationResult(successful = true)
        var lastNameResult : ValidationResult? = null

        val response : MutableStateFlow<Response<String>>

        if (command == Constants.Command.ADD){
            response = _registerResponse
             emailResult = validateEmailUseCase.invoke(registerState.value.email)
        } else {
            response = _updateResponse
        }
        response.value = Response.Loading

        val phoneResult = validatePhoneUseCase.invoke(registerState.value.phone)
        val firstNameResult = validateNameUseCase.invoke(registerState.value.fname)
        if (registerState.value.lname != null && registerState.value.lname != "")
            lastNameResult = registerState.value.lname?.let { validateNameUseCase.invoke(it) }

        val hasError = listOf(
            emailResult,
            phoneResult,
            firstNameResult,
            lastNameResult ?: ValidationResult( successful = true)
        ).any { !it.successful }


        if(hasError) {
            _registerState.value = registerState.value.copy(
                emailError = emailResult.errorMessage,
                phoneError = phoneResult.errorMessage,
                fnameError = firstNameResult.errorMessage,
                lnameError = lastNameResult?.errorMessage,
            )
            response.value = Response.Error(Exception("Field(s) error"))
            return
        } else {
            _registerState.value = registerState.value.copy(
                emailError = null,
                phoneError = null,
                fnameError = null,
                lnameError = null
            )
        }
        register()
//        if (command == Constants.Command.ADD){
//
//        } else {
////            update()
//        }

    }

    private fun register() {
        val account = getStaffAccount(
            registerState.value.fname,
            registerState.value.lname,
            registerState.value.phone,
            registerState.value.email,
            registerState.value.address,
            registerState.value.birthday)

        registerStaffUseCase.invoke(
            registerState.value.email,
            account,
        ) {
            _registerResponse.value = it
        }
    }

//    private fun update() {
//        val newAccount = user.copy(
//            first_name = registerState.value.fname,
//            last_name = registerState.value.lname ?: "",
//            phone = registerState.value.phone,
//            address = registerState.value.address ?: "",
//            birthday = registerState.value.birthday,
//        )
//        editAccountUseCase(newAccount, registerState.value.image) {
//            _updateResponse.value = it
//        }
//    }

    private fun getStaffAccount(
        first_name: String,
        last_name: String?,
        phone: String,
        email: String,
        address: String?,
        birthday: Date?
    ) : StaffAccount {
        return StaffAccount(
            "",
            first_name,
            last_name?: "",
            phone,
            email,
            address?:"",
            birthday,
            null,
            null,
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