package com.example.fyp.account_management.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

abstract class Account(
        open var id: String = "",
        open val first_name: String = "",
        open val last_name: String = "",
        open val phone: String = "",
        open val email: String = "",
        open val address: String = "",
        open val birthday: Date? = null,
        open var profileImagePath : String? = null,
        open var profileUri : String? = null,
        open val accountType: AccountType = AccountType.Customer,
        @ServerTimestamp
        open val dateCreated: Date? = null
)

enum class AccountType (val accessLevel: Int){
        Customer(0),
        Admin(1),
        Manager(2),
        KitchenStaff(3),
        Staff(4)
}