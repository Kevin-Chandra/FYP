package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOngoingOrderFromRemoteUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    ) {
    suspend operator fun invoke(result: (Flow<Response<List<Order>>>) -> Unit) {
        result.invoke(orderRepository.getOrderListByStatus(listOf(
            OrderStatus.Ongoing,
            OrderStatus.Sent,
            OrderStatus.Confirmed
        )))
    }
}