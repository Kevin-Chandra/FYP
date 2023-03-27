package com.example.fyp.account_management.data.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Account(
        var id: String = "",
        val first_name: String = "",
        val last_name: String = "",
        val phone: String = "",
        val email: String = "",
        val address: String = "",
        val birthday: Date? = null,
        var profileImagePath : String? = null,
        var profileUri : String? = null,
        val accountType: AccountType = AccountType.Customer,
        @ServerTimestamp
        val dateCreated: Date? = null,
        val orderHistory: List<String>? = null,
        var staffPosition: StaffPosition? = null
)

enum class AccountType (val accessLevel: Int){
        Customer(0),
        Admin(1),
        Manager(2),
        KitchenStaff(3),
        Staff(4)
}
enum class StaffPosition{
        Disabled,
        Pending,
        Regular,
        Kitchen
}
enum class SpecialAccess (){
        MANAGE_STAFF
}

//sealed class AccountTypeSealed {
//        object Customer : AccountTypeSealed()
//        object Admin : AccountTypeSealed()
//        object Staff : AccountTypeSealed()
//        object KitchenStaff : AccountTypeSealed()
//        object Manager : AccountTypeSealed()
//}