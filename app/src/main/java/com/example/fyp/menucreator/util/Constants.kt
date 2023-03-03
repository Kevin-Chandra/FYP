package com.example.fyp.menucreator.util

object FireStoreCollection{
    const val FOOD = "Food"
    const val MODIFIER = "Modifier"
    const val MODIFIER_ITEM = "ModifierItem"
    const val FOOD_CATEGORY = "FoodCategory"
    const val USER = "User"
}

object FireStoreDocumentField{
    const val ID = "id"
    const val FOOD_CATEGORY_NAME = "name"
    val DATE = "date"
    val PRODUCT_ID = "productId"
}

object NavigationCommand{
    val ADD = "add"
    val EDIT = "edit"
}