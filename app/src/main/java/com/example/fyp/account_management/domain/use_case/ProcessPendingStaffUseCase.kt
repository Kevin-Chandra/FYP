package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.AccountType
import com.example.fyp.account_management.data.model.StaffPosition
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import javax.inject.Inject

class ProcessPendingStaffUseCase @Inject constructor(
    private val authRepository: AuthRepository
    ) {
    operator fun invoke(account: Account, position: StaffPosition,approvingAccount: Account, result: (Response<String>) -> Unit){
        if (!(approvingAccount.accountType == AccountType.Manager || approvingAccount.accountType == AccountType.Admin)){
            result.invoke(Response.Error(Exception("You have no permission!")))
            return
        }
        val updatedAccount = account.copy(
            staffPosition = position
        )
        authRepository.updateProfile(updatedAccount,null,result)
    }
}