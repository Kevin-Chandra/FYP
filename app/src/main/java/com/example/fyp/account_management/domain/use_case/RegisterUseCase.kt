package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        account: Account,
        result: (Response<String>) -> Unit
    ) = authRepository.registerUser(email,password,account,result)
}