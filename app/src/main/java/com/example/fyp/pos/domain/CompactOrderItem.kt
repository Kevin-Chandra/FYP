package com.example.fyp.pos.domain

import com.example.fyp.account_management.util.Response
import com.example.fyp.ordering_system.data.model.Order
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.model.OrderStatus
import com.example.fyp.ordering_system.data.repository.remote.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CompactOrderItem @Inject constructor(
) {
    operator fun invoke(itemList: List<OrderItem>,result: (List<OrderItem>) -> Unit) {
        // map of foodId,orderItem
        val map = mutableMapOf<String,MutableList<OrderItem>>()
        itemList.forEach {
            if (!map.containsKey(it.foodId)){
                map[it.foodId] = mutableListOf(it)
            } else {
                val orderItemList = map[it.foodId]!!

                if (it.modifierItems.isNullOrEmpty()){
                    val itemToAdd = orderItemList.find { it1 -> it1.modifierItems.isNullOrEmpty() }!!
                    orderItemList.remove(itemToAdd)
                    orderItemList.add(itemToAdd.copy(
                        quantity = itemToAdd.quantity + it.quantity,
                        price = itemToAdd.price + it.price
                    ))
                } else {

                    val itemToAdd = orderItemList.find { it1 -> it1.modifierItems == it.modifierItems }
                    if (itemToAdd == null){
                        orderItemList.add(it)
                    } else {
                        orderItemList.remove(itemToAdd)
                        orderItemList.add(itemToAdd.copy(
                            quantity = itemToAdd.quantity + it.quantity,
                            price = itemToAdd.price + it.price)
                        )
                    }
                }
            }
        }
        result.invoke(map.values.flatMap { it.toList() })
    }
}