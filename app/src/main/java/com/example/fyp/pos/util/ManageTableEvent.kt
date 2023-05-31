package com.example.fyp.pos.util

import com.example.fyp.pos.data.model.Table

sealed class ManageTableEvent {
    data class OnFinishTable(val id: String) : ManageTableEvent()
    data class AssignTable(val id: String, val pax: Int, val label :String? = null) : ManageTableEvent()
    data class OnResetTable(val id: String) : ManageTableEvent()
    data class OnDeleteTable(val id: String) : ManageTableEvent()
    data class OnSetTableAvailability(val id: String, val availability : Boolean) : ManageTableEvent()
}