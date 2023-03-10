package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(result: () -> Unit) = authRepository.logout(result)
}