package com.example.fyp.pos.util

data class AddEditTableState(
    val tableNumber: String = "",
    val tableNumberError: String? = null,
    val tableName: String = "",
    val tableNameError: String? = null,
    val tablePaxCapacity: Int = 1,
    val edit: Boolean = false,
    val success: Boolean = false,
    val successMessage: String? = null,
    val loading: Boolean = false,
    val error: String? = null
)