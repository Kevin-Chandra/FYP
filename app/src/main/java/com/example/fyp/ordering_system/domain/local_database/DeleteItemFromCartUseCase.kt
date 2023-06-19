package com.example.fyp.ordering_system.domain.local_database

import com.example.fyp.ordering_system.data.repository.local.OrderItemDao
import javax.inject.Inject

class DeleteItemFromCartUseCase @Inject constructor(
    private val orderItemDao: OrderItemDao
    ) {
    suspend operator fun invoke(id: String) = orderItemDao.deleteByOrderItemId(id)
}