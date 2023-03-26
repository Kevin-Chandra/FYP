package com.example.fyp.account_management.domain.use_case

import android.net.Uri
import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.data.repository.StaffRepository
import com.example.fyp.account_management.util.Response
import java.lang.Exception
import javax.inject.Inject

class SetRegisterStaffTokenUseCase @Inject constructor(
    private val staffRepository: StaffRepository
) {
    suspend operator fun invoke(
        token: String,
        result: (Response<String>) -> Unit
    ) {
        if (token.isBlank()){
            result.invoke(Response.Error(Exception("Token is blank")))
            return
        }
        staffRepository.setToken(token,result)
    }
}