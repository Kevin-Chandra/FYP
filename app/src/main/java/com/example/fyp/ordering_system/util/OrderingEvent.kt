package com.example.fyp.ordering_system.util

sealed class OrderingEvent {
    data class FoodDeletedChanged(val id: String) : OrderingEvent()
    data class SubmitOrder(val accountId: String): OrderingEvent()
}