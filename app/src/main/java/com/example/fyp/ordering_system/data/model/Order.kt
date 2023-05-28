package com.example.fyp.ordering_system.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Order(
    val orderId : String = "",
    val orderType : OrderType = OrderType.Online,
    val orderStatus: OrderStatus = OrderStatus.Sent,
    val orderList: List<String> = listOf(),
    val orderStartTime: Date= Date(),
    val orderFinishTime: Date= Date(),
    val taxPercentage: Double = 0.0,
    val serviceChargePercentage : Double = 0.0,
    val subTotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    val orderBy: String = "",
    val pax: Int = 0,
    val tableNumber: String = "Online",
    val tableId: String? = null,
    val paidStatus: Boolean = false,
)

enum class OrderType{
    Online,
    DineIn,
    Reservation,
    Takeaway
}

enum class OrderStatus{
    Sent,
    Rejected,
    Confirmed,
    Ongoing,
    Finished
}