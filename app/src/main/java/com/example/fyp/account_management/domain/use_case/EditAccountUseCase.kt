package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.model.CustomerAccount
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class EditAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        newAccount: CustomerAccount,
        result: (Response<String>) -> Unit
    ) = authRepository.updateProfile(newAccount,result)
}