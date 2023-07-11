package com.example.fyp.account_management.domain.use_case

import com.example.fyp.account_management.data.repository.AuthRepository
import javax.inject.Inject

class GetSessionFlowUseCase @Inject constructor(
    private val authRepository: AuthRepository
    ) {
    suspend operator fun invoke() = authRepository.getSessionByFlow()
}