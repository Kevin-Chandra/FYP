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
    const val DATE = "date"
    const val PRODUCT_ID = "productId"
    const val ACCOUNT_TYPE = "accountType"
    const val PROFILE_IMAGE_PATH = "profileImagePath"
}

object NavigationCommand{
    val ADD = "add"
    val EDIT = "edit"
}

object FirebaseStorageReference{
    const val PRODUCT_IMAGE_REFERENCE = "productImages/"
    const val PROfILE_IMAGE_REFERENCE = "profileImages/"
}