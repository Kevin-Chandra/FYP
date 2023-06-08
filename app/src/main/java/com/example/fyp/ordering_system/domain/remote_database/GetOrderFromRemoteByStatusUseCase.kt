package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOrderFromRemoteByStatusUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    ) {
    suspend operator fun invoke(statuses: List<OrderStatus>,lastHours: Long = 0 , result: (Flow<Response<List<Order>>>) -> Unit) {
        result.invoke(orderRepository.getOrderListByStatus(statuses,lastHours))
    }
}