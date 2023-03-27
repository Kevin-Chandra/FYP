package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.data.repository.StaffRepository
import com.example.fyp.account_management.util.Response
import javax.inject.Inject

class ProcessPendingStaffUseCase @Inject constructor(
    private val authRepository: AuthRepository
    ) {
    operator fun invoke(account: Account, position: StaffPosition, result: (Response<String>) -> Unit){
        val updatedAccount = account.copy(
            staffPosition = position
        )
        authRepository.updateProfile(updatedAccount,null,result)
    }
}