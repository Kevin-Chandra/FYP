package com.example.fyp.pos.domain

import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import javax.inject.Inject

class UpdateOrderHistoryUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(accountId: String,orderId: String, result: (Response<String>) -> Unit) {
        authRepository.updateUserOrderHistory(accountId,orderId,result)
    }
}