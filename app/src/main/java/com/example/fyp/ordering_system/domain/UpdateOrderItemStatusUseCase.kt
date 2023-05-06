package com.example.fyp.ordering_system.domain

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import javax.inject.Inject

class UpdateOrderItemStatusUseCase @Inject constructor(
    private val orderRepository: OrderItemRepository,
    ) {
    suspend operator fun invoke(id: String, itemStatus: OrderItemStatus, result: (Response<String>) -> Unit) {
        orderRepository.updateItemStatus(id,itemStatus,result)
    }
}