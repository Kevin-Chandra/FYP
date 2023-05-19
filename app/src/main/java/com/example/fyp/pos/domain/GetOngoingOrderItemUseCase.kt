package com.example.fyp.pos.domain

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOngoingOrderItemUseCase @Inject constructor(
    private val orderItemRepository: OrderItemRepository,
    ) {
    suspend operator fun invoke(result: (Flow<Response<List<OrderItem>>>) -> Unit) {
        result.invoke(orderItemRepository.getOrderItemListByStatus(listOf(
            OrderItemStatus.Confirmed,
            OrderItemStatus.Preparing,
            OrderItemStatus.Finished
        )))
    }
}