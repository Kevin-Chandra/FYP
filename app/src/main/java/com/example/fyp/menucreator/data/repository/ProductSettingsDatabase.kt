package com.example.fyp.menucreator.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fyp.menucreator.data.model.ProductSettings

@Database(entities = [ProductSettings::class], exportSchema = false, version = 1)
abstract class ProductSettingsDatabase: RoomDatabase() {

    abstract fun productSettingsDao(): ProductSettingsDao

}