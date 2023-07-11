package com.example.fyp.account_management.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Account(
        val id: String = "",
        val first_name: String = "",
        val last_name: String = "",
        val phone: String = "",
        val email: String = "",
        val address: String = "",
        val birthday: Date? = null,
        val profileImagePath : String? = null,
        val profileUri : String? = null,
        val accountType: AccountType = AccountType.Customer,
        @ServerTimestamp
        val dateCreated: Date? = null,
        val orderHistory: List<String>? = null,
        var staffPosition: StaffPosition? = null
)

enum class AccountType {
        Customer,
        Admin,
        Manager,
        Staff
}
enum class StaffPosition{
        Disabled,
        Pending,
        Regular,
        Kitchen
}
