package com.example.fyp.ordering_system.util

import com.example.fyp.menucreator.data.model.Food
import com.example.fyp.menucreator.data.model.Modifier
import com.example.fyp.menucreator.data.model.ModifierItem

sealed class AddToCartEvent {
    data class FoodChanged(val food: Food,val requiredModifier: Int) : AddToCartEvent()

    //map of modifier id to list of item id
    data class ModifierItemListChanged(val modifier: Modifier, val list: List<ModifierItem>) : AddToCartEvent()

    data class QuantityChanged(val qty: Int) : AddToCartEvent()

    data class NoteChanged(val note: String) : AddToCartEvent()

    object RequiredModifierUnavailable : AddToCartEvent()

    object AddToCart: AddToCartEvent()
}