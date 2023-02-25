package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class EditAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(newAccount: Account,
                        result: (Response<String>) -> Unit
    ) = authRepository.updateProfile(newAccount,result)
}