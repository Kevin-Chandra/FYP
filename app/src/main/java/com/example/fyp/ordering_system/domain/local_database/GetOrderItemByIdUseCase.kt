package com.example.fyp.ordering_system.domain.local_database

import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.repository.local.OrderItemDao
import javax.inject.Inject

class GetOrderItemByIdUseCase @Inject constructor(
    private val orderItemDao: OrderItemDao
    ) {
    suspend operator fun invoke(id: String) = orderItemDao.getOrderItem(id)
}