package com.example.fyp.account_management.util

import java.util.Date

sealed class RegistrationEvent {
    data class EmailChanged(val email: String) : RegistrationEvent()
    data class PasswordChanged(val password: String) : RegistrationEvent()
    data class FirstNameChanged(val fname: String): RegistrationEvent()
    data class LastNameChanged(val lname: String): RegistrationEvent()
    data class PhoneChanged(val phone: String): RegistrationEvent()
    data class AddressChanged(val address: String): RegistrationEvent()
    data class BirthdayChanged(val date: Date) : RegistrationEvent()
    object Submit: RegistrationEvent()
}