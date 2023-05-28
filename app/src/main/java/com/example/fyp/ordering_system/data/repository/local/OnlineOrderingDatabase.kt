package com.example.fyp.ordering_system.data.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fyp.ordering_system.data.model.OrderItem

@Database(entities = [OrderItem::class], version = 1)
abstract class OnlineOrderingDatabase: RoomDatabase() {
    abstract fun orderItemDao(): OrderItemDao

}