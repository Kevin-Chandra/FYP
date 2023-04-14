package com.example.fyp.menucreator.util

import android.net.Uri
import com.example.fyp.account_management.data.model.Account
import java.util.Date

sealed class AddEditModifierEvent {

    data class ProductIdChanged(val id: String) : AddEditModifierEvent()

    data class NameChanged(val name: String) : AddEditModifierEvent()

    data class RequiredChanged(val required: Boolean): AddEditModifierEvent()

    data class MultipleChoiceChanged(val multipleChoice: Boolean): AddEditModifierEvent()

    data class ItemListChanged(val items:List<Triple<Pair<String,Boolean>,String,String>>): AddEditModifierEvent()

    data class ItemErrorListChanged(val itemErrors:List<Triple<String?,String?,String?>?>): AddEditModifierEvent()

    data class ImageChanged(val image: Uri?) : AddEditModifierEvent()

    data class Save(val isEdit: Boolean,val account: Account): AddEditModifierEvent()
}