package com.example.fyp.pos.domain

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import javax.inject.Inject

class PayOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(orderId:String, result: (Response<String>) -> Unit) {
        orderRepository.updateOrder(orderId, mapOf(FireStoreDocumentField.PAID_STATUS to true),result)
    }
}