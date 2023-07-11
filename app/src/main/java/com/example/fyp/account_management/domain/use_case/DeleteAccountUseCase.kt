package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(password: String,result: (Response<String>) -> Unit) = authRepository.deleteAccount(password,result)
}