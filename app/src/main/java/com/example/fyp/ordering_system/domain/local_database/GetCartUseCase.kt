package com.example.fyp.ordering_system.domain.local_database

import com.example.fyp.ordering_system.data.repository.local.OrderItemDao
import javax.inject.Inject

class GetCartUseCase @Inject constructor(
    private val orderItemDao: OrderItemDao
    ) {
    operator fun invoke() = orderItemDao.getOrderItemList()
}