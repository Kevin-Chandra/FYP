package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOrderFromRemoteByOrderIdUseCase @Inject constructor(
    private val repository: OrderRepository,
    ) {
    suspend operator fun invoke(orderId: String, result: (Response<Order>) -> Unit) {
        repository.getOrder(orderId,result)
    }
}