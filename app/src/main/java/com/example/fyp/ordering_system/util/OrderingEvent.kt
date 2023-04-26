package com.example.fyp.ordering_system.util

import android.net.Uri
import java.util.Date

sealed class OrderingEvent {
    data class FoodIdChanged(val id: String) : OrderingEvent()

    //map of modifier id to list of item id
    data class ModifierItemListChanged(val list: Map<String,List<String>>) : OrderingEvent()

    data class QuantityChanged(val qty: Int) : OrderingEvent()

    object AddToCart: OrderingEvent()
}