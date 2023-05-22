package com.example.fyp.pos.data.model

import com.google.firebase.firestore.DocumentId

data class Table(
    @DocumentId
    val id: String = "",
    val tableNumber: String = "",
    val name: String = "",
    val pax: Int = 0,
    val paxCapacity: Int = 0,
    val currentOrder: String? = null,
    val tableStatus: TableStatus = TableStatus.Vacant
) {

}