package com.example.fyp.menucreator.util

import android.net.Uri
import java.util.*

data class AddEditModifierState(
    val productId: String = "",
    val productIdError: String? = null,
    val name: String = "",
    val nameError: String? = null,
    val isMultipleChoice: Boolean = false,
    val isRequired: Boolean = false,
    // Triple of <Id, isEdit?> , Name, Price
    val itemList: List<Triple<Pair<String,Boolean>,String,String>> = listOf(),
    val itemErrorList: List<Triple<String?,String?,String?>?> = listOf(),
    val image: Uri? = null
)