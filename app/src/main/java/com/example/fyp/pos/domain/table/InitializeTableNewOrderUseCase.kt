package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderType
import com.example.fyp.ordering_system.domain.remote_database.SubmitOrderUseCase
import com.example.fyp.pos.data.repository.TableRepository
import javax.inject.Inject

class InitializeTableNewOrderUseCase @Inject constructor(
    private val tableRepository: TableRepository,
    private val submitOrderUseCase: SubmitOrderUseCase
    ) {
    suspend operator fun invoke(newOrder: Order, tableId: String, result: (Response<String>) -> Unit) {
        tableRepository.newOrder(tableId,newOrder.orderId,result)
        submitOrderUseCase(newOrder.copy(
            orderType = OrderType.DineIn,
        ),null,result)
    }
}