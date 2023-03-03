package com.example.fyp.menucreator.data.model

import com.google.firebase.firestore.DocumentId

data class FoodCategory(
    @DocumentId
    val id: String = "",
    val name: String = "",
)