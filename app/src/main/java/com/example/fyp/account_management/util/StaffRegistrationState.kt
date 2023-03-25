package com.example.fyp.account_management.util

import android.net.Uri
import java.util.*

data class StaffRegistrationState(
    val email: String = "",
    val emailError: String? = null,
    val fname: String = "",
    val fnameError: String? = null,
    val lname: String? = null,
    val lnameError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val address: String? = null,
    val birthday: Date? = null
)