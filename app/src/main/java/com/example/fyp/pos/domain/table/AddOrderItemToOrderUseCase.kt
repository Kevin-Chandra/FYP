package com.example.fyp.pos.domain.table

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class AddOrderItemToOrderUseCase @Inject constructor(
    private val orderItemRepository: OrderItemRepository,
    private val orderRepository: OrderRepository,
    ) {
    suspend operator fun invoke(orderId: String, orderItemList: List<OrderItem>, result: (Response<String>) -> Unit) {
        orderItemList.forEach{
            CoroutineScope(Dispatchers.IO).launch {
                orderItemRepository.addItem(it.copy(
                    timeAdded = Date(),
                    orderItemStatus = OrderItemStatus.Confirmed,
                    orderId = orderId
                ),result)
            }
        }
        orderRepository.addOrderItem(orderId,orderItemList.map { it.orderItemId },result)

    }
}