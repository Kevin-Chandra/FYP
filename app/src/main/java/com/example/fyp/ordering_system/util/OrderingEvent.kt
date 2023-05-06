package com.example.fyp.ordering_system.util

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem

sealed class OrderingEvent {
    data class FoodDeletedChanged(val id: String) : OrderingEvent()
    object SubmitOrder: OrderingEvent()
}