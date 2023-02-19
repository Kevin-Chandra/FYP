package com.example.fyp.menucreator.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import kotlin.collections.ArrayList

data class Modifier(
    override val productId: String = "",
    override val name : String = "",
    val multipleChoice : Boolean = false,
    val required : Boolean = false,
    val modifierItemList : ArrayList<String> = arrayListOf(),
    @ServerTimestamp
    val date: Date = Date()
    ) : Product(productId, ProductType.Modifier)