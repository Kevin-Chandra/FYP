package com.example.fyp.ordering_system.domain.local_database

import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.repository.local.OrderItemDao
import java.util.UUID
import javax.inject.Inject

class UpdateOrderItemIdInCartUseCase @Inject constructor(
    private val orderItemDao: OrderItemDao
    ) {
    suspend operator fun invoke(orderItemList: List<OrderItem>){
        orderItemDao.nukeTable()
        orderItemList.forEach {
            val newItem = it.copy(orderItemId = UUID.randomUUID().toString())
            orderItemDao.upsertOrderItem(newItem)
        }
    }
}