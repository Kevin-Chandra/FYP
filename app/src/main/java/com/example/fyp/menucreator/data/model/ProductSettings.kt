package com.example.fyp.menucreator.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fyp.menucreator.util.FireStoreDocumentField
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "product_settings")
data class ProductSettings(
    @PrimaryKey
    @DocumentId
    val id: String = FireStoreDocumentField.PRODUCT_SETTINGS,
    val tax: Double = 0.0,
    val serviceCharge: Double = 0.0
)