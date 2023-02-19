package com.example.fyp.menucreator.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList

data class Food(
    override val productId: String = "",
    override val name: String = "",
    override val price: Double = -1.0,
    val description: String = "",
    val category: FoodCategory? = null,
    val modifiable: Boolean = false,
    val allTimeSales: Int = -1,
    val modifierList: ArrayList<String> = ArrayList<String>(),
    @ServerTimestamp
    val date: Date = Date()
) : Product(productId, ProductType.FoodAndBeverage)