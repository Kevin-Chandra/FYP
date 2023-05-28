package com.example.fyp.pos.domain.table_ordering

import com.example.fyp.pos.data.repository.local.PosOrderItemDao
import javax.inject.Inject

class PosGetCartUseCase @Inject constructor(
    private val orderItemDao: PosOrderItemDao
    ) {
    operator fun invoke() = orderItemDao.getOrderItemList()
}