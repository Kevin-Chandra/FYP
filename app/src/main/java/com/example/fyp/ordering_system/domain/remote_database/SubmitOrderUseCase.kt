package com.example.fyp.ordering_system.domain.remote_database

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderItemStatus
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.model.OrderType
import com.example.fyp.ordering_system.data.model.OrderType.*
import com.example.fyp.ordering_system.data.repository.remote.OrderItemRepository
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class SubmitOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    ) {
    suspend operator fun invoke(order: Order, orderItemList: List<OrderItem>?, result: (Response<String>) -> Unit) {
        orderItemList?.forEach{
            CoroutineScope(Dispatchers.IO).launch {
                orderItemRepository.addItem(it.copy(
                    timeAdded = Date(),
                    orderItemStatus = OrderItemStatus.Sent,
                    orderId = order.orderId
                ),result)
            }
        }
        val newOrder = when(order.orderType){
            Online -> {
                order.copy(
                    orderStartTime =  Date(),
                    orderStatus = OrderStatus.Sent
                )
            }
            DineIn -> {
                order.copy(
                    orderStartTime =  Date(),
                    orderStatus = OrderStatus.Ongoing
                )
            }
            Reservation -> order
            Takeaway -> order
        }
        orderRepository.addOrder(newOrder,result)
    }
}