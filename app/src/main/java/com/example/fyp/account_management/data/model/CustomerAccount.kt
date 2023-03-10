package com.example.fyp.account_management.data.model

import java.util.*

data class CustomerAccount(
    override var id: String = "",
    override val first_name: String = "",
    override val last_name: String = "",
    override val phone: String = "",
    override val email: String = "",
    override val address: String = "",
    override val birthday: Date? = null,
    override var profileUri: String? = null,
    override var profileImagePath: String? = null,
    override val dateCreated: Date? = null
) : Account(
    id,
    first_name,
    last_name,
    phone,
    email,
    address,
    birthday,
    profileImagePath,
    profileUri,
    AccountType.Customer,
    dateCreated
)