package com.example.fyp.ordering_system.data.repository.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.fyp.ordering_system.data.model.OrderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {

    @Upsert
    suspend fun upsertOrderItem(orderItem: OrderItem)

    @Query("SELECT * FROM order_items WHERE orderItemId = :orderItemId")
    suspend fun getOrderItem(orderItemId: String): OrderItem

    @Query("SELECT * FROM order_items ORDER BY timeAdded ASC")
    fun getOrderItemList(): Flow<List<OrderItem>>

    @Delete
    suspend fun deleteOrderItem(item: OrderItem)

}