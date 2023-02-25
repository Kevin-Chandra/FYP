package com.example.fyp.account_management.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.data.model.CustomerAccount
import com.example.fyp.account_management.domain.use_case.EditAccountUseCase
import com.example.fyp.account_management.domain.use_case.RegisterUseCase
import com.example.fyp.account_management.domain.use_case.ValidateEmailUseCase
import com.example.fyp.account_management.domain.use_case.ValidatePasswordUseCase
import com.example.fyp.account_management.util.RegistrationEvent
import com.example.fyp.account_management.util.RegistrationState
import com.example.fyp.account_management.util.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import javax.inject.Inject


/**
 * This view model served to register and edit account
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val editAccountUseCase: EditAccountUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase
    ) : ViewModel(){

    private val _registerState = MutableStateFlow(RegistrationState())
    val registerState = _registerState.asStateFlow()

    private val _registerResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val registerResponse = _registerResponse.asStateFlow()

    fun onEvent(event: RegistrationEvent) {
        when(event) {
            is RegistrationEvent.EmailChanged -> {
                _registerState.value = registerState.value.copy(email = event.email)
            }
            is RegistrationEvent.PasswordChanged -> {
                _registerState.value = registerState.value.copy(password = event.password)
            }
            is RegistrationEvent.FirstNameChanged -> {
                _registerState.value = registerState.value.copy(fname = event.fname)
            }
            is RegistrationEvent.LastNameChanged -> {
                _registerState.value = registerState.value.copy(lname = event.lname)
            }
            is RegistrationEvent.PhoneChanged -> {
                _registerState.value = registerState.value.copy(phone = event.phone)
            }
            is RegistrationEvent.BirthdayChanged -> {
                _registerState.value = registerState.value.copy(birthday = event.date)
            }
            is RegistrationEvent.AddressChanged -> {
                _registerState.value = registerState.value.copy(address = event.address)
            }
            is RegistrationEvent.Submit -> {
                submitData()
            }
        }
        println(event)
    }

    private fun submitData() {
        _registerResponse.value = Response.Loading
        val emailResult = validateEmailUseCase.invoke(registerState.value.email)
        val passwordResult = validatePasswordUseCase.invoke(registerState.value.password)
        //TODO add more validate use case

        val hasError = listOf(
            emailResult,
            passwordResult
        ).any { !it.successful }

        if(hasError) {
            _registerState.value = registerState.value.copy(
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage
            )
            _registerResponse.value = Response.Error(Exception("Field(s) is invalid!"))
            return
        }

        //success validation
        val account = getAccount(
                    registerState.value.fname,
                    registerState.value.lname,
                    registerState.value.phone,
                    registerState.value.email,
                    registerState.value.address,
                    registerState.value.birthday)

        viewModelScope.launch {
            registerUseCase.invoke(
                registerState.value.email,
                registerState.value.password,
                account
            ) {
                _registerResponse.value = it
            }
        }
    }

    private fun getAccount(
        first_name: String,
        last_name: String?,
        phone: String,
        email: String,
        address: String?,
        birthday: Date?
    ) : CustomerAccount {
        return CustomerAccount(
            "",
            first_name,
            last_name?: "",
            phone,
            email,
            address?:"",
            birthday
        )
    }


    sealed class ValidationEvent {
        object Success: ValidationEvent()
    }
}