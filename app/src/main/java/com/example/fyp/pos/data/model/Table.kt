package com.example.fyp.pos.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Table(
    @DocumentId
    val id: String = "",
    @ServerTimestamp
    val dateCreated : Date = Date(),
    val tableNumber: Int = -1,
    val name: String = "",
    val label: String? = null,
    val pax: Int = 0,
    val paxCapacity: Int = 0,
    val currentOrder: String? = null,
    val tableStatus: TableStatus = TableStatus.Available
) {

}