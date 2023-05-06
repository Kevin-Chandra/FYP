package com.example.fyp.menucreator.util

object FireStoreCollection{
    const val FOOD = "Food"
    const val MODIFIER = "Modifier"
    const val MODIFIER_ITEM = "ModifierItem"
    const val FOOD_CATEGORY = "FoodCategory"
    const val USER = "User"
    const val ADMIN_SETTINGS = "AdminSettings"
    const val ORDER_ITEM = "OrderItem"
    const val ORDER = "Order"
}

object FireStoreDocumentField{
    const val STAFF_REGISTRATION_TOKEN = "StaffRegistrationToken"
    const val ID = "id"
    const val FOOD_CATEGORY_NAME = "name"
    const val ORDER_ID = "orderId"
    const val ORDER_ITEM_ID = "orderItemId"
    const val ORDER_ITEM_STATUS = "orderItemStatus"
    const val ORDER_STATUS = "orderStatus"
    const val DATE = "date"
    const val TIME_ADDED = "timeAdded"
    const val PRODUCT_ID = "productId"
    const val AVAILABILITY = "availability"
    const val ACCOUNT_TYPE = "accountType"
    const val CREATED_BY = "createdBy"
    const val STAFF_STATUS = "staffPosition"
    const val PROFILE_IMAGE_PATH = "profileImagePath"
    const val PROFILE_URI = "profileUri"
    const val PRODUCT_IMAGE_PATH = "imagePath"
    const val PRODUCT_IMAGE_URI = "imageUri"
    const val MODIFIER_ITEM_LIST = "modifierItemList"
    const val SETTINGS = "Settings"
}

object NavigationCommand{
    const val ADD = "add"
    const val EDIT = "edit"
}

object FirebaseStorageReference{
    const val PRODUCT_IMAGE_REFERENCE = "productImages/"
    const val FOOD_IMAGE_PATH = "foods/"
    const val MODIFIER_IMAGE_PATH = "modifiers/"
    const val PROfILE_IMAGE_REFERENCE = "profileImages/"
}

object MenuCreatorResponse{
    const val ITEM_DELETED = "Item deleted"
    const val MODIFIER_DELETED = "Modifier deleted"
    const val MODIFIER_ITEM_ADD_SUCCESS = "Modifier item upload success"
    const val MODIFIER_ADD_SUCCESS = "Modifier added successfully"
    const val MODIFIER_UPLOAD_SUCCESS = "Modifier upload success"
    const val MODIFIER_MERGE_SUCCESS = "Modifier upload success"
    const val MODIFIER_UPDATE_SUCCESS = "Modifier update success"
    const val FOOD_DELETED = "Food deleted"
    const val PRODUCT_IMAGE_DELETED = "Image deleted"
}