package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderType
import com.example.fyp.ordering_system.domain.remote_database.SubmitOrderUseCase
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.repository.remote.TableRepository
import javax.inject.Inject

class InitializeTableNewOrderUseCase @Inject constructor(
    private val tableRepository: TableRepository,
    private val submitOrderUseCase: SubmitOrderUseCase
    ) {
    suspend operator fun invoke(order : Order = Order(), table: Table, result: (Response<String>) -> Unit) {
        val newOrder = order.copy(
            orderType = OrderType.DineIn,
            tableId = table.id,
            tableNumber = table.tableNumber.toString()
        )
        tableRepository.newOrder(table.id,newOrder.orderId,result)
        submitOrderUseCase(newOrder,null,result)
    }
}