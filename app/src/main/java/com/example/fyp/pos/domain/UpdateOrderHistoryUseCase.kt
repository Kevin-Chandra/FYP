package com.example.fyp.pos.domain

import com.example.fyp.account_management.data.repository.AuthRepository
import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateOrderHistoryUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(accountId: String,orderId: String, result: (Response<String>) -> Unit) {
        authRepository.updateUserOrderHistory(accountId,orderId,result)
    }
}