package com.example.fyp.pos.util

import com.example.fyp.menucreator.data.model.Food

sealed class TableOrderEvent {
    data class OnAddFood(val food: Food, val quantity: Int) : TableOrderEvent()
    data class OnDeleteOrderItem(val id: String) : TableOrderEvent()
    object SubmitOrder : TableOrderEvent()
}