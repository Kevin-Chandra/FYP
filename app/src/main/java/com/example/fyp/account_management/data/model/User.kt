package com.example.fyp.account_management.data.model

import java.util.*

abstract class Account(
        open var id: String = "",
        open val first_name: String = "",
        open val last_name: String = "",
        open val phone: String = "",
        open val email: String = "",
        open val address: String = "",
        open val birthday: Date? = null,
        open val accountType: AccountType = AccountType.Customer
)

enum class AccountType (val accessLevel: Int){
        Customer(0),
        Admin(1),
        Manager(2),
        KitchenStaff(3),
        Staff(4)
}