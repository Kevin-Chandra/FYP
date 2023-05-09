package com.example.fyp.menucreator.data.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fyp.menucreator.data.model.ProductSettings
import com.example.fyp.menucreator.util.FireStoreDocumentField
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: ProductSettings)

    @Query("UPDATE product_settings SET serviceCharge=:amount WHERE id = :id ")
    suspend fun updateServiceCharge(amount: Double, id: String = FireStoreDocumentField.PRODUCT_SETTINGS)

    @Query("UPDATE product_settings SET tax=:amount WHERE id = :id ")
    suspend fun updateTax(amount: Double, id: String = FireStoreDocumentField.PRODUCT_SETTINGS)

    @Query("SELECT serviceCharge FROM product_settings WHERE id = :id")
    suspend fun getServiceCharge(id: String = FireStoreDocumentField.PRODUCT_SETTINGS) : Double

    @Query("SELECT tax FROM product_settings WHERE id = :id")
    suspend fun getTax(id: String = FireStoreDocumentField.PRODUCT_SETTINGS) : Double

    @Query("SELECT * FROM product_settings WHERE id = :id")
    suspend fun getSettings(id: String = FireStoreDocumentField.PRODUCT_SETTINGS) : ProductSettings
}