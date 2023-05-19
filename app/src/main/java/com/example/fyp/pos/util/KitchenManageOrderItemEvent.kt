package com.example.fyp.pos.util

import com.example.fyp.ordering_system.data.model.Order

sealed class KitchenManageOrderItemEvent {
    data class OnPrepareOrderItem(val itemId: String) : KitchenManageOrderItemEvent()
    data class OnFinishOrderItem(val itemId: String) : KitchenManageOrderItemEvent()
}