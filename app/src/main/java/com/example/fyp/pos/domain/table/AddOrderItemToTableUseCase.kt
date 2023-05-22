package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderType
import com.example.fyp.ordering_system.domain.remote_database.SubmitOrderUseCase
import com.example.fyp.pos.data.model.Table
import com.example.fyp.pos.data.repository.TableRepository
import javax.inject.Inject

class AddOrderItemToTableUseCase @Inject constructor(
    private val addOrderItemToOrderUseCase: AddOrderItemToOrderUseCase,
    ) {
    suspend operator fun invoke(itemList: List<OrderItem>, table: Table, result: (Response<String>) -> Unit) {
        if (table.currentOrder.isNullOrEmpty()){
            result(Response.Error( Exception("Order not initialized!")))
            return
        }
        addOrderItemToOrderUseCase(table.currentOrder,itemList,result)
    }
}