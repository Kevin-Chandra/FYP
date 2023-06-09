package com.example.fyp.ordering_system.domain.local_database

import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.repository.local.OrderItemDao
import javax.inject.Inject

class UpsertToCartUseCase @Inject constructor(
    private val orderItemDao: OrderItemDao
    ) {
    suspend operator fun invoke(orderItem: OrderItem, update: Boolean = false){
        val list = orderItemDao.getOrderItemByFoodId(orderItem.foodId)
        if (list.isEmpty()){
            orderItemDao.upsertOrderItem(orderItem)
        } else {
            val item = list.find { orderItem.modifierItems == it.modifierItems }

            if (item == null){
                orderItemDao.upsertOrderItem(orderItem)
            } else {
                if (update){
                    val newItem =  if (item.orderItemId == orderItem.orderItemId){
                        item.copy(
                            quantity = orderItem.quantity,
                            price = orderItem.price,
                            note = orderItem.note
                        )
                    } else {
                        item.copy(
                            quantity = orderItem.quantity + item.quantity,
                            price = orderItem.price + item.price,
                            note = orderItem.note + item.note
                        )
                    }
                    orderItemDao.deleteByOrderItemId(orderItem.orderItemId)
                    orderItemDao.upsertOrderItem( newItem )
                } else {
                    orderItemDao.deleteByOrderItemId(item.orderItemId)
                    orderItemDao.upsertOrderItem(orderItem.copy(
                        quantity = orderItem.quantity + item.quantity,
                        price = orderItem.price + item.price,
                        note = orderItem.note + item.note
                    ))
                }

            }
        }
    }
}