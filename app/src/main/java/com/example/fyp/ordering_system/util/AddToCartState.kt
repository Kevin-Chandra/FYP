package com.example.fyp.ordering_system.util

import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem

data class AddToCartState(
    val foodId: String = "",
    val modifierList : MutableMap<Modifier,List<ModifierItem>?> = mutableMapOf(),
    val quantity : Int  = 1,
    val price : Double = 0.0,
    val note: String = ""
)