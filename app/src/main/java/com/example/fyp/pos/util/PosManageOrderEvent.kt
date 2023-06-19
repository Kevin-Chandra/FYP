package com.example.fyp.pos.util

import com.example.fyp.ordering_system.data.model.Order

sealed class PosManageOrderEvent {
    data class OnFinishOrderItem(val order: Order) : PosManageOrderEvent()
}