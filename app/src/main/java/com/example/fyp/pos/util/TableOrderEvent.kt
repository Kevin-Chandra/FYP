package com.example.fyp.pos.util

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.ordering_system.data.model.OrderItem

sealed class TableOrderEvent {
    data class OnAddOrderItem(val orderItem: OrderItem) : TableOrderEvent()
    data class OnAddFood(val food: Food, val quantity: Int) : TableOrderEvent()
    data class OnDeleteOrderItem(val id: String) : TableOrderEvent()
    data class OnIncrementQuantity(val orderItem: OrderItem,val quantity: Int) : TableOrderEvent()
    data class OnDecrementQuantity(val orderItem: OrderItem,val quantity: Int) : TableOrderEvent()
    object SubmitOrder : TableOrderEvent()
}