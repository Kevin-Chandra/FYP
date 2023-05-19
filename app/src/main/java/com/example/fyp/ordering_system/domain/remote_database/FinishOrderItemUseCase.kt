package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import javax.inject.Inject

class FinishOrderItemUseCase @Inject constructor(
    private val orderRepository: OrderItemRepository,
    ) {
    suspend operator fun invoke(id: String, result: (Response<String>) -> Unit) {
        orderRepository.orderItemFinish(id,result)
    }
}