package com.example.fyp.pos.util

import com.example.fyp.ordering_system.data.model.Order

sealed class PosManageOrderEvent {
//    data class OnPrepareItem(val itemId: String) : ManageOrderEvent()
    data class OnFinishOrderItem(val order: Order) : PosManageOrderEvent()
}