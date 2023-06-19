package com.example.fyp.menucreator.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Modifier(
    override val productId: String = "",
    override val name : String = "",
    val multipleChoice : Boolean = false,
    val required : Boolean = false,
    val modifierItemList : List<String> = listOf(),
    val minItem: Int? = 0,
    val maxItem: Int? = 0,
    val imagePath: String? = null,
    val imageUri: String? = null,
    @ServerTimestamp
    @Exclude
    val createdAt: Date? = null,
    val lastUpdated: Date = Date(),
    val createdBy: String = "",
    val lastUpdatedBy: String = "",
    ) : Product(productId, ProductType.Modifier)