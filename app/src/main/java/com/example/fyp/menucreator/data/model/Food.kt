package com.example.fyp.menucreator.data.model

import android.net.Uri
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList

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
    val modifierList: ArrayList<String> = ArrayList(),
    val lastUpdated: Date = Date(),
    @ServerTimestamp
    @Exclude
    val createdAt: Date? = null
) : Product(productId, ProductType.FoodAndBeverage)