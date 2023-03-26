package com.example.fyp.menucreator.util

object FireStoreCollection{
    const val FOOD = "Food"
    const val MODIFIER = "Modifier"
    const val MODIFIER_ITEM = "ModifierItem"
    const val FOOD_CATEGORY = "FoodCategory"
    const val USER = "User"
    const val ADMIN_SETTINGS = "AdminSettings"
}

object FireStoreDocumentField{
    const val STAFF_REGISTRATION_TOKEN = "StaffRegistrationToken"
    const val ID = "id"
    const val FOOD_CATEGORY_NAME = "name"
    const val DATE = "date"
    const val PRODUCT_ID = "productId"
    const val ACCOUNT_TYPE = "accountType"
    const val STAFF_STATUS = "staffPosition"
    const val PROFILE_IMAGE_PATH = "profileImagePath"
    const val SETTINGS = "Settings"
}

object NavigationCommand{
    val ADD = "add"
    val EDIT = "edit"
}

object FirebaseStorageReference{
    const val PRODUCT_IMAGE_REFERENCE = "productImages/"
    const val PROfILE_IMAGE_REFERENCE = "profileImages/"
}