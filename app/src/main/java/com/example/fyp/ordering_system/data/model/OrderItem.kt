package com.example.fyp.ordering_system.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.fyp.ordering_system.util.Converters
import com.google.firebase.firestore.DocumentId
import java.util.Date


@Entity (tableName = "order_items")
@TypeConverters(Converters::class)
data class OrderItem (
    val timeAdded: Date = Date(),
    @PrimaryKey val orderItemId : String = "",
    val foodId : String = "",
    val modifierItems : List<String>? = null,
    val quantity: Int = -1,
    val note : String? = null,
    val timeFinished: Date = Date(),
    val orderItemStatus :OrderItemStatus = OrderItemStatus.Created
)

enum class OrderItemStatus{
    Created,
    Sent,
    Preparing,
    Finished,
    Cancelled
}