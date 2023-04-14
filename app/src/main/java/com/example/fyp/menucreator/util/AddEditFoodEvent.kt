package com.example.fyp.menucreator.util

import android.net.Uri
import com.example.fyp.account_management.data.model.Account
import java.util.Date

sealed class AddEditFoodEvent {

    data class ProductIdChanged(val id: String) : AddEditFoodEvent()

    data class NameChanged(val name: String) : AddEditFoodEvent()

    data class PriceChanged(val price: String): AddEditFoodEvent()

    data class DescriptionChanged(val description: String): AddEditFoodEvent()

    data class FoodCategoryChanged(val category: String): AddEditFoodEvent()

    data class ModifierChanged(val modifierList: List<String>?): AddEditFoodEvent()

    data class ModifiableChanged(val isModifiable: Boolean): AddEditFoodEvent()

    data class ImageChanged(val image: Uri?) : AddEditFoodEvent()

    data class Save(val isEdit: Boolean, val account: Account): AddEditFoodEvent()
}