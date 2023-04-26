package com.example.fyp.ordering_system.util

import android.net.Uri
import com.example.fyp.ordering_system.data.model.OrderItem
import java.util.Date

sealed class CartEvent {
    data class OrderItemListChanged(val orderItem: List<OrderItem>) : CartEvent()

    object Submit: CartEvent()
}