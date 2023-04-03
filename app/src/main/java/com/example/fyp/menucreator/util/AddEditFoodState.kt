package com.example.fyp.menucreator.util

import android.net.Uri
import java.util.*

data class AddEditFoodState(
    val productId: String = "",
    val productIdError: String? = null,
    val name: String = "",
    val nameError: String? = null,
    val price: String = "",
    val priceError: String? = null,
    val description: String? = null,
    val descriptionError: String? = null,
    val foodCategory: String = "",
    val foodCategoryError: String? = null,
    val isModifiable: Boolean = false,
    val modifierList: List<String>? = null,
    val image: Uri? = null
)