package com.example.fyp.account_management.ui.view_model

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.*
import com.example.fyp.account_management.domain.use_case.*
import com.example.fyp.account_management.util.Constants
import com.example.fyp.account_management.util.RegistrationEvent
import com.example.fyp.account_management.util.RegistrationState
import com.example.fyp.account_management.util.Response
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
class AuthViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val editAccountUseCase: EditAccountUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateNameUseCase: ValidateNameUseCase,
    private val validatePhoneUseCase: ValidatePhoneUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val getSessionUseCase: GetSessionUseCase,
//    savedStateHandle: SavedStateHandle
    ) : ViewModel(){

    private var command: String = Constants.Command.ADD

    private var _user: Account? = null
    val user get() = _user!!

    private val _loadingState = MutableStateFlow<Response<Boolean>>(Response.Loading)
    val loadingState = _loadingState.asStateFlow()

    private val _registerState = MutableStateFlow(RegistrationState())
    val registerState = _registerState.asStateFlow()

    private val _registerResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val registerResponse = _registerResponse.asStateFlow()

    private val _updateResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val updateResponse = _updateResponse.asStateFlow()

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
            is RegistrationEvent.ImageChanged -> {
                _registerState.value = registerState.value.copy(image = event.image)
            }
            is RegistrationEvent.Submit -> {
                submitData()
            }
        }
    }

    private fun submitData() {
        var emailResult = ValidationResult(successful = true)
        var passwordResult = ValidationResult(successful = true)
        var lastNameResult : ValidationResult? = null

        val response : MutableStateFlow<Response<String>>

        if (command == Constants.Command.ADD){
            response = _registerResponse
             emailResult = validateEmailUseCase.invoke(registerState.value.email)
             passwordResult = validatePasswordUseCase.invoke(registerState.value.password)
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
            passwordResult,
            phoneResult,
            firstNameResult,
            lastNameResult ?: ValidationResult( successful = true)
        ).any { !it.successful }


        if(hasError) {
            _registerState.value = registerState.value.copy(
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage,
                phoneError = phoneResult.errorMessage,
                fnameError = firstNameResult.errorMessage,
                lnameError = lastNameResult?.errorMessage,
            )
            response.value = Response.Error(Exception("Field(s) error"))
            return
        } else {
            _registerState.value = registerState.value.copy(
                emailError = null,
                passwordError = null,
                phoneError = null,
                fnameError = null,
                lnameError = null
            )
        }

        if (command == Constants.Command.ADD){
            register()
        } else {
            update()
        }

    }

    private fun register() {
        val account = getCustomerAccount(
            registerState.value.fname,
            registerState.value.lname,
            registerState.value.phone,
            registerState.value.email,
            registerState.value.address,
            registerState.value.birthday)

        registerUseCase.invoke(
            registerState.value.email,
            registerState.value.password,
            account,
            registerState.value.image
        ) {
            _registerResponse.value = it
        }
    }

    private fun update() {
        val newAccount: Account?
        newAccount = user.copy(
            first_name = registerState.value.fname,
            last_name = registerState.value.lname ?: "",
            phone = registerState.value.phone,
            address = registerState.value.address ?: "",
            birthday = registerState.value.birthday,
        )

        val img = if (registerState.value.image.toString() == user.profileUri) null else registerState.value.image
        editAccountUseCase(newAccount, img) {
            _updateResponse.value = it
        }
    }

    private fun getCustomerAccount(
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
            AccountType.Customer,
            Date()
        )
    }

    fun getSession() = viewModelScope.launch{
        if (_user == null) {
            reset()
            _user = getSessionUseCase()
            if (_user != null) {
                loadToRegisterState()
                command = Constants.Command.EDIT
                _loadingState.value = Response.Success(true)
            } else {
                _loadingState.value = Response.Error(Exception("User Not Found"))
            }
        }
    }

    private fun loadToRegisterState() {
        onEvent(RegistrationEvent.AddressChanged(user.address))
        onEvent(RegistrationEvent.PhoneChanged(user.phone))
        onEvent(RegistrationEvent.FirstNameChanged(user.first_name))
        onEvent(RegistrationEvent.LastNameChanged(user.last_name))
        user.profileUri?.toUri()?.let { RegistrationEvent.ImageChanged(it) }?.let { onEvent(it) }
        user.birthday?.let { RegistrationEvent.BirthdayChanged(it) }?.let { onEvent(it) }
    }

    private fun reset(){
        _registerState.value = RegistrationState()
        _updateResponse.value = Response.Success("")
        _registerResponse.value = Response.Success("")
        _loadingState.value = Response.Loading
    }

}