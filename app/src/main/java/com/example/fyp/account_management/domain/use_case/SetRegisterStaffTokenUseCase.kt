package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.repository.StaffRepository
import com.example.fyp.account_management.util.Response
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