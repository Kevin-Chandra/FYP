package com.example.fyp.pos.util

sealed class KitchenManageOrderItemEvent {
    data class OnPrepareOrderItem(val itemId: String) : KitchenManageOrderItemEvent()
    data class OnFinishOrderItem(val itemId: String) : KitchenManageOrderItemEvent()
}