package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.account_management.data.repository.StaffRepository
import com.example.fyp.account_management.util.Response
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProcessPendingStaffUseCase @Inject constructor(
    private val staffRepository: StaffRepository
    ) {
    operator fun invoke(account: Account, position: StaffPosition, result: (Response<String>) -> Unit){
        val updatedAccount = account.copy(
            staffPosition = position
        )
        staffRepository.updateProfile(updatedAccount,null,result)
    }
}