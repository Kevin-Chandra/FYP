package com.example.fyp.pos.util

import com.example.fyp.pos.data.model.Table

sealed class ManageTableEvent {
    data class OnAddTable(val table: Table) : ManageTableEvent()
}