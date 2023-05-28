package com.example.fyp.pos.domain.table_ordering

import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.repository.local.OrderItemDao
import com.example.fyp.pos.data.repository.local.PosOrderItemDao
import javax.inject.Inject

class DeleteItemFromPosCartUseCase @Inject constructor(
    private val orderItemDao: PosOrderItemDao
    ) {
    suspend operator fun invoke(id: String) = orderItemDao.deleteByOrderItemId(id)
}