package com.example.fyp.account_management.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.account_management.domain.use_case.*
import com.example.fyp.account_management.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import javax.inject.Inject


/**
 * This view model served to register and edit account
 */
@HiltViewModel
class StaffViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val getPendingStaffUseCase: GetPendingStaffUseCase,
    private val getStaffListUseCase: GetStaffListUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateNameUseCase: ValidateNameUseCase,
    private val validatePhoneUseCase: ValidatePhoneUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val getTokenUseCase: GetRegisterStaffTokenUseCase,
    private val processPendingStaffUseCase: ProcessPendingStaffUseCase,
    private val getSessionUseCase: GetSessionUseCase
    ) : ViewModel(){

    // TODO RECHECK ALL register staff

    private var _user: Account? = null
    val user get() = _user!!

    private var approvingUser: Account? = null

    private var token: String = ""

    private val _pendingAccounts = MutableStateFlow<Response<List<Account>>>(Response.Loading)
    val pendingAccounts = _pendingAccounts.asStateFlow()

    private val _staffAccounts = MutableStateFlow<Response<List<Account>>>(Response.Loading)
    val staffAccounts = _staffAccounts.asStateFlow()

    private val _loadingState = MutableStateFlow<Response<Boolean>>(Response.Loading)
    val loadingState = _loadingState.asStateFlow()

    private val _registerState = MutableStateFlow(StaffRegistrationState())
    val registerState = _registerState.asStateFlow()

    private val _registerResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val registerResponse = _registerResponse.asStateFlow()

    private val _updateResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val updateResponse = _updateResponse.asStateFlow()

    private val _processPendingResponse = MutableStateFlow<Response<String>>(Response.Success(""))
    val processPendingResponse = _processPendingResponse.asStateFlow()

    init {
        getSession()
        getPendingStaff()
        getStaffList()
        getToken()
    }

    private fun getSession() = viewModelScope.launch{
        approvingUser = getSessionUseCase()
    }

    fun onEvent(event: StaffRegistrationEvent) {
        when(event) {
            is StaffRegistrationEvent.TokenChanged -> {
                _registerState.value = registerState.value.copy(token = event.token)
            }
            is StaffRegistrationEvent.EmailChanged -> {
                _registerState.value = registerState.value.copy(email = event.email)
            }
            is StaffRegistrationEvent.PasswordChanged -> {
                _registerState.value = registerState.value.copy( password = event.password)
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
            is StaffRegistrationEvent.ImageChanged -> {
                _registerState.value = registerState.value.copy(image = event.uri)
            }
            is StaffRegistrationEvent.Submit -> {
                submitData()
            }
        }
    }

    private fun submitData() {
        _registerResponse.value = Response.Loading

        val tokenResult = if (registerState.value.token != token){
            ValidationResult(false, "Invalid Token")
        } else {
            ValidationResult(true)
        }
        val emailResult = validateEmailUseCase.invoke(registerState.value.email)
        val passwordResult = validatePasswordUseCase.invoke(registerState.value.password)
        val phoneResult = validatePhoneUseCase.invoke(registerState.value.phone)
        val firstNameResult = validateNameUseCase.invoke(registerState.value.fname)
        var lastNameResult : ValidationResult? = null
        if (registerState.value.lname != null && registerState.value.lname != "")
            lastNameResult = registerState.value.lname?.let { validateNameUseCase.invoke(it) }

        val hasError = listOf(
            tokenResult,
            emailResult,
            passwordResult,
            phoneResult,
            firstNameResult,
            lastNameResult ?: ValidationResult( successful = true)
        ).any { !it.successful }


        if(hasError) {
            _registerState.value = registerState.value.copy(
                tokenError = tokenResult.errorMessage,
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage,
                phoneError = phoneResult.errorMessage,
                fnameError = firstNameResult.errorMessage,
                lnameError = lastNameResult?.errorMessage,
            )
            _registerResponse.value = Response.Error(Exception("Field(s) error"))
            return
        } else {
            _registerState.value = registerState.value.copy(
                tokenError = null,
                emailError = null,
                passwordError = null,
                phoneError = null,
                fnameError = null,
                lnameError = null
            )
        }
        register()
    }

    private fun register() {
        val account = getStaffAccount(
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
            Date(),
            null,
            StaffPosition.Pending
        )
    }

    private fun getToken() = getTokenUseCase.invoke {
        viewModelScope.launch {
            it.collect() {
                token = when (it) {
                    is Response.Success -> {
                        it.data
                    }
                    else -> {
                        ""
                    }
                }
            }
        }
    }

    private fun getPendingStaff() = getPendingStaffUseCase.invoke {
        viewModelScope.launch {
            it.collect {
                _pendingAccounts.value = it
            }
        }
    }

    private fun getStaffList() = getStaffListUseCase.invoke {
        viewModelScope.launch {
            it.collect {
                _staffAccounts.value = it
            }
        }
    }

    fun acceptPendingStaff(account: Account) = viewModelScope.launch{
        _processPendingResponse.value = Response.Loading
        if (approvingUser == null){
            _processPendingResponse.value = Response.Error(Exception("Approving Account Uninitialized!"))
            return@launch
        }
        processPendingStaffUseCase.invoke(account,StaffPosition.Regular,approvingUser!!){
            _processPendingResponse.value = it
        }
    }

    fun acceptPendingStaff(account: Account, position: StaffPosition) = viewModelScope.launch{
        _processPendingResponse.value = Response.Loading
        if (approvingUser == null){
            _processPendingResponse.value = Response.Error(Exception("Approving Account Uninitialized!"))
            return@launch
        }
        processPendingStaffUseCase.invoke(account,position,approvingUser!!){
            _processPendingResponse.value = it
        }
    }

    fun rejectPendingStaff(account: Account) = viewModelScope.launch{
        _processPendingResponse.value = Response.Loading
        if (approvingUser == null){
            _processPendingResponse.value = Response.Error(Exception("Approving Account Uninitialized!"))
            return@launch
        }
        processPendingStaffUseCase.invoke(account,StaffPosition.Disabled,approvingUser!!){
            _processPendingResponse.value = it
        }
    }

    fun loadStaff(account: Account){
        _user = account
    }



}