package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import javax.inject.Inject

class GetOrderItemFromRemoteByOrderIdByReturnUseCase @Inject constructor(
    private val repository: OrderItemRepository,
    ) {
    suspend operator fun invoke(orderId: String) : Response<List<OrderItem>> {
        return repository.getOrderItemListByOrderIdByReturn(orderId)
    }
}