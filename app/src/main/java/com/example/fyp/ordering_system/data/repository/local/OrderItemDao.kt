package com.example.fyp.ordering_system.data.repository.local

import androidx.room.Dao
import androidx.room.Delete
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

    @Query("SELECT * FROM order_items WHERE foodId = :foodId")
    suspend fun getOrderItemByFoodId(foodId: String): List<OrderItem>

    @Query("SELECT * FROM order_items ORDER BY timeAdded ASC")
    fun getOrderItemList(): Flow<List<OrderItem>>

    @Query("DELETE FROM order_items WHERE orderItemId = :id")
    suspend fun deleteByOrderItemId(id: String)

    @Delete
    suspend fun deleteOrderItem(item: OrderItem)

    @Query("DELETE FROM order_items")
    suspend fun nukeTable()


}