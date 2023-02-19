package com.example.fyp.menucreator.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*


data class ModifierItem(
    override val productId: String = "",
    override val name:String = "",
    override val price:Double = -1.0,
    @ServerTimestamp
    val date: Date = Date()
): Product(productId, ProductType.ModifierItem)