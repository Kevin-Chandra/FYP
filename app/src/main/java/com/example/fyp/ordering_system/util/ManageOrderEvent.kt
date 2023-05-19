package com.example.fyp.ordering_system.util

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem
import com.example.fyp.ordering_system.data.model.Order

sealed class ManageOrderEvent {
    data class OnAcceptOrder(val orderId: String,val list: List<String>) : ManageOrderEvent()
    data class OnRejectOrder(val orderId: String,val list: List<String>) : ManageOrderEvent()
    data class OnDeleteOrder(val order: Order) : ManageOrderEvent()
}