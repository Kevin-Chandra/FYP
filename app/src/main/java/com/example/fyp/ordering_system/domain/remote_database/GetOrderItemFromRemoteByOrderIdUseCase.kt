package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOrderItemFromRemoteByOrderIdUseCase @Inject constructor(
    private val repository: OrderItemRepository,
    ) {
    suspend operator fun invoke(orderId: String, result: (Flow<Response<List<OrderItem>>>) -> Unit) {
        result.invoke(repository.getOrderItemListByOrderId(orderId))
    }
}