package com.example.fyp.pos.domain

import com.example.fyp.account_management.util.Response
import com.example.fyp.menucreator.data.repository.FoodRepository
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.example.fyp.menucreator.util.UiState
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PayOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(orderId:String, result: (Response<String>) -> Unit) {
        orderRepository.updateOrder(orderId, mapOf(FireStoreDocumentField.PAID_STATUS to true),result)
    }
}