package com.example.fyp.pos.data.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fyp.ordering_system.data.model.OrderItem
import com.example.fyp.ordering_system.data.repository.local.OrderItemDao

@Database(entities = [OrderItem::class], version = 1)
abstract class PosOrderingDatabase: RoomDatabase() {

    abstract fun orderItemDao(): PosOrderItemDao

}