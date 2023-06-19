package com.example.fyp.menucreator.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Food(
    override val productId: String = "",
    override val name: String = "",
    override val price: Double = -1.0,
    val description: String = "",
    val category: String = "",
    val modifiable: Boolean = false,
    val imagePath: String? = null,
    val imageUri: String? = null,
    val allTimeSales: Int = -1,
    val modifierList: List<String> = listOf(),
    val lastUpdated: Date = Date(),
    @ServerTimestamp
    @Exclude
    val createdAt: Date? = null,
    val availability : Boolean = true,
    val createdBy: String = "",
    val lastUpdatedBy: String = "",
) : Product(productId, ProductType.FoodAndBeverage)