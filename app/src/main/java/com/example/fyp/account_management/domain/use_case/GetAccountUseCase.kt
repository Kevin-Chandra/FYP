package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
    ) {
    suspend operator fun invoke(id: String, result: (Response<Account>) -> Unit) = authRepository.getAccount(id,result)
}