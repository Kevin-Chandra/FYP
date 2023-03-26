package com.example.fyp.account_management.domain.use_case

import android.net.Uri
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.data.repository.StaffRepository
import com.example.fyp.account_management.util.Response
import javax.inject.Inject

class RegisterStaffUseCase @Inject constructor(
    private val staffRepository: StaffRepository
) {
    operator fun invoke(
        email: String,
        password: String,
        account: Account,
        result: (Response<String>) -> Unit
    ) = staffRepository.registerStaff(email,password,account,result)
}