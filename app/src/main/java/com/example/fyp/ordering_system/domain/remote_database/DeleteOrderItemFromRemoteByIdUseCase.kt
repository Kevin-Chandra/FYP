package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import javax.inject.Inject

class DeleteOrderItemFromRemoteByIdUseCase @Inject constructor(
    private val orderItemRepository: OrderItemRepository,
    ) {
    suspend operator fun invoke(id: String, result: (Response<String>) -> Unit) {
        orderItemRepository.deleteOrderItem(id,result)
    }
}