package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import javax.inject.Inject

class FinishOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    ) {
    suspend operator fun invoke(id: String, result: (Response<String>) -> Unit) {
        orderRepository.finishOrder(id,result)
    }
}