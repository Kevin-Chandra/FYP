package com.example.fyp.account_management.util

import android.net.Uri
import java.util.Date

sealed class StaffRegistrationEvent {
    data class TokenChanged(val token: String) : StaffRegistrationEvent()
    data class EmailChanged(val email: String) : StaffRegistrationEvent()
    data class PasswordChanged(val password: String) : StaffRegistrationEvent()
    data class FirstNameChanged(val fname: String): StaffRegistrationEvent()
    data class LastNameChanged(val lname: String): StaffRegistrationEvent()
    data class PhoneChanged(val phone: String): StaffRegistrationEvent()
    data class AddressChanged(val address: String): StaffRegistrationEvent()
    data class BirthdayChanged(val date: Date) : StaffRegistrationEvent()
    data class ImageChanged(val uri: Uri) : StaffRegistrationEvent()
    object Submit: StaffRegistrationEvent()
}