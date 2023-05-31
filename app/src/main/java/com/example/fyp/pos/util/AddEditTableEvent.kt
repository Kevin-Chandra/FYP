package com.example.fyp.pos.util

sealed class AddEditTableEvent {
    data class OnTableNumberChanged(val tableNumber: String) : AddEditTableEvent()
    data class OnTableNameChanged(val name: String) : AddEditTableEvent()
    data class OnPaxCapacityChanged(val pax: Int) : AddEditTableEvent()
    object OnSave : AddEditTableEvent()
    object OnReset : AddEditTableEvent()
}