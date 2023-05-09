package com.example.fyp.ordering_system.domain.validation

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteOrderFromRemoteUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    ) {
    suspend operator fun invoke(orderStatus: OrderStatus, result: (Flow<Response<List<Order>>>) -> Unit) {
        //TODO kdadmslml
    }
}