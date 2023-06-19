package com.example.fyp.ordering_system.util

import com.example.fyp.account_management.data.model.Account
import com.example.fyp.ordering_system.data.model.Order

sealed class ManageOrderEvent {
    data class OnAcceptOrder(val account : Account, val orderId: String, val list: List<String>) : ManageOrderEvent()
    data class OnRejectOrder(val account : Account, val orderId: String,val list: List<String>) : ManageOrderEvent()
    data class OnDeleteOrder(val account : Account, val order: Order) : ManageOrderEvent()
}